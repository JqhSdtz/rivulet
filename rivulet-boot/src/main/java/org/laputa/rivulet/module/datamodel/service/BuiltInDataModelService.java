package org.laputa.rivulet.module.datamodel.service;

import com.google.common.collect.Streams;
import liquibase.Liquibase;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.exception.LiquibaseException;
import liquibase.ext.hibernate.GlobalSetting;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import lombok.extern.slf4j.Slf4j;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.util.RedissonLockUtil;
import org.laputa.rivulet.module.datamodel.entity.*;
import org.laputa.rivulet.module.datamodel.entity.column_relation.*;
import org.laputa.rivulet.module.datamodel.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static liquibase.structure.core.ForeignKeyConstraintType.importedKeyCascade;

/**
 * 系统内部的数据模型自动同步到数据模型对应表中
 * 需要注意的是，系统内部的表、字段等数据模型改名字后无法和之前的关联，即之前对应的数据都将丢失
 * 所以系统内部表和字段一般情况下禁止改名字
 * @author JQH
 * @since 下午 6:47 22/09/04
 */
@Service
@Order(3)
@Slf4j
public class BuiltInDataModelService implements ApplicationRunner {
    @Resource
    private JpaProperties jpaProperties;
    @Resource
    private RedissonLockUtil redissonLockUtil;
    @Resource
    private RvPrototypeRepository rvPrototypeRepository;
    @Resource
    private RvColumnRepository rvColumnRepository;
    @Resource
    private RvIndexRepository rvIndexRepository;
    @Resource
    private RvForeignKeyRepository rvForeignKeyRepository;
    @Resource
    private RvUniqueConstraintRepository rvUniqueConstraintRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) throws Exception {
        log.info("检测内部实体类变更——开始");
        // 禁止liquibase-hibernate显示found column/index/...的信息，太多了
        GlobalSetting.setShowFoundInfo(false);
        DiffResult hibernateDiffResult = getHibernateDiff();
        Set<? extends DatabaseObject> hibernateObjects = hibernateDiffResult.getMissingObjects();
        List<Table> tableList = new ArrayList<>();
        hibernateObjects.forEach(databaseObject -> {
            if (databaseObject instanceof Table) {
                tableList.add((Table) databaseObject);
            }
        });
        Result result = redissonLockUtil.doWithLock("checkBuiltInDataModel", () -> {
            List<RvPrototype> originalPrototypeList = rvPrototypeRepository.findAll();
            List<String> deletedPrototypeIdList = new ArrayList<>();
            Map<String, RvPrototype> rvPrototypeMap = new HashMap<>(tableList.size());
            originalPrototypeList.forEach(prototype -> {
                if (tableList.stream().filter(table -> table.getName().equals(prototype.getCode())).findAny().isPresent()) {
                    rvPrototypeMap.put(prototype.getCode(), prototype);
                } else {
                    log.info("删除表 " + prototype.getCode());
                    deletedPrototypeIdList.add(prototype.getId());
                }
            });
            rvPrototypeRepository.deleteAllById(deletedPrototypeIdList);
            List<RvPrototype> targetRvPrototypeList = new ArrayList<>(tableList.size());
            // 先把所有的prototype建好，便于构造过程中的外键引用
            tableList.forEach(table -> {
                if (!rvPrototypeMap.containsKey(table.getName())) {
                    rvPrototypeMap.put(table.getName(), new RvPrototype());
                }
            });
            tableList.forEach(table -> {
                targetRvPrototypeList.add(buildRvPrototype(table, rvPrototypeMap));
            });
            rvPrototypeRepository.saveAll(targetRvPrototypeList);
            return Result.succeed();
        });
        if (!result.isSuccessful()) {
            throw result.toRawException();
        }
        log.info("检测内部实体类变更——结束");
    }

    private RvPrototype buildRvPrototype(Table table, Map<String, RvPrototype> rvPrototypeMap) {
        RvPrototype rvPrototype = rvPrototypeMap.get(table.getName());
        boolean isNew = rvPrototype.getId() == null;
        rvPrototype.setCode(table.getName());
        if (isNew) {
            log.info("新增表 " + table.getName());
            rvPrototype.setName(table.getName());
        }
        if (rvPrototype.getRemark() == null) {
            rvPrototype.setRemark(table.getRemarks());
        }

        Map<String, RvColumn> rvColumnMap = new HashMap<>(table.getColumns().size());
        List<String> deletedColumnIdList = new ArrayList<>();
        if (rvPrototype.getColumns() != null) {
            rvPrototype.getColumns().forEach(rvColumn -> {
                if (table.getColumns().stream().filter(column -> column.getName().equals(rvColumn.getCode())).findAny().isPresent()) {
                    rvColumnMap.put(rvColumn.getCode(), rvColumn);
                } else {
                    log.info("表 " + table.getName() + " 删除字段 " + rvColumn.getCode());
                    deletedColumnIdList.add(rvColumn.getId());
                }
            });
        }
        rvColumnRepository.deleteAllById(deletedColumnIdList);
        rvPrototype.setColumns(table.getColumns().stream().map(column -> {
            RvColumn rvColumn = buildRvColumn(column, rvColumnMap);
            rvColumn.setPrototype(rvPrototype);
            return rvColumn;
        }).collect(Collectors.toList()));

        Map<String, RvIndex> rvIndexMap = new HashMap<>(table.getIndexes().size());
        List<String> deletedIndexIdList = new ArrayList<>();
        if (rvPrototype.getIndexes() != null) {
            rvPrototype.getIndexes().forEach(rvIndex -> {
                if (table.getIndexes().stream().filter(index -> index.getName().equals(rvIndex.getCode())).findAny().isPresent()) {
                    rvIndexMap.put(rvIndex.getCode(), rvIndex);
                } else {
                    log.info("表 " + table.getName() + " 删除索引 " + rvIndex.getCode());
                    deletedIndexIdList.add(rvIndex.getId());
                }
            });
        }
        rvIndexRepository.deleteAllById(deletedIndexIdList);
        rvPrototype.setIndexes(Streams.mapWithIndex(table.getIndexes().stream(), (index, idx) -> {
            RvIndex rvIndex = buildRvIndex(index, rvColumnMap, rvIndexMap);
            rvIndex.setOrderNum((int) idx);
            rvIndex.setPrototype(rvPrototype);
            return rvIndex;
        }).collect(Collectors.toList()));

        rvPrototype.setPrimaryKey(buildRvPrimaryKey(table.getPrimaryKey(), rvPrototype, rvColumnMap, rvIndexMap));

        Map<String, RvForeignKey> rvForeignKeyMap = new HashMap<>(table.getOutgoingForeignKeys().size());
        List<String> deletedForeignKeyIdList = new ArrayList<>();
        if (rvPrototype.getForeignKeys() != null) {
            rvPrototype.getForeignKeys().forEach(rvForeignKey -> {
                if (table.getOutgoingForeignKeys().stream().filter(foreignKey -> foreignKey.getName().equals(rvForeignKey.getCode())).findAny().isPresent()) {
                    rvForeignKeyMap.put(rvForeignKey.getCode(), rvForeignKey);
                } else {
                    log.info("表 " + table.getName() + " 删除外键 " + rvForeignKey.getCode());
                    deletedForeignKeyIdList.add(rvForeignKey.getId());
                }
            });
        }
        rvForeignKeyRepository.deleteAllById(deletedForeignKeyIdList);
        rvPrototype.setForeignKeys(Streams.mapWithIndex(table.getOutgoingForeignKeys().stream(), (foreignKey, idx) -> {
            RvForeignKey rvForeignKey = buildRvForeignKey(foreignKey, rvPrototypeMap, rvColumnMap, rvIndexMap, rvForeignKeyMap);
            rvForeignKey.setOrderNum((int) idx);
            rvForeignKey.setPrototype(rvPrototype);
            return rvForeignKey;
        }).collect(Collectors.toList()));

        Map<String, RvUniqueConstraint> rvUniqueConstraintMap = new HashMap<>(table.getUniqueConstraints().size());
        List<String> deletedUniqueConstraintIdList = new ArrayList<>();
        if (rvPrototype.getUniqueConstraints() != null) {
            rvPrototype.getUniqueConstraints().forEach(rvUniqueConstraint -> {
                if (table.getUniqueConstraints().stream().filter(uniqueConstraint -> uniqueConstraint.getName().equals(rvUniqueConstraint.getCode())).findAny().isPresent()) {
                    rvUniqueConstraintMap.put(rvUniqueConstraint.getCode(), rvUniqueConstraint);
                } else {
                    log.info("表 " + table.getName() + " 删除唯一性约束 " + rvUniqueConstraint.getCode());
                    deletedUniqueConstraintIdList.add(rvUniqueConstraint.getId());
                }
            });
        }
        rvUniqueConstraintRepository.deleteAllById(deletedUniqueConstraintIdList);
        rvPrototype.setUniqueConstraints(Streams.mapWithIndex(table.getUniqueConstraints().stream(), (uniqueConstraint, idx) -> {
            RvUniqueConstraint rvUniqueConstraint = buildRvUniqueConstraint(uniqueConstraint, rvColumnMap, rvIndexMap, rvUniqueConstraintMap);
            rvUniqueConstraint.setOrderNum((int) idx);
            rvUniqueConstraint.setPrototype(rvPrototype);
            return rvUniqueConstraint;
        }).collect(Collectors.toList()));
        rvPrototype.setDbSyncFlag(true);
        return rvPrototype;
    }

    private RvColumn buildRvColumn(Column column, Map<String, RvColumn> rvColumnMap) {
        RvColumn rvColumn = rvColumnMap.get(column.getName());
        boolean isNew = rvColumn == null;
        if (isNew) {
            rvColumn = new RvColumn();
            log.info("表 " + column.getRelation().getName() + " 新增字段 " + column.getName());
            rvColumnMap.put(column.getName(), rvColumn);
            rvColumn.setName(column.getName());
        }
        rvColumn.setCode(column.getName());
        rvColumn.setDataType(column.getType().toString());
        rvColumn.setIsNullable(column.isNullable());
        if (rvColumn.getOrderNum() == null) {
            rvColumn.setOrderNum(column.getOrder());
        }
        if (column.getDefaultValue() != null) {
            rvColumn.setDefaultValue(column.getDefaultValue().toString());
        }
        if (rvColumn.getRemark() == null) {
            rvColumn.setRemark(column.getRemarks());
        }
        return rvColumn;
    }

    private RvIndex buildRvIndex(Index index, Map<String, RvColumn> rvColumnMap, Map<String, RvIndex> rvIndexMap) {
        RvIndex rvIndex = rvIndexMap.get(index.getName());
        boolean isNew = rvIndex == null;
        if (isNew) {
            rvIndex = new RvIndex();
            log.info("表 " + index.getRelation().getName() + " 新增索引 " + index.getName());
            rvIndexMap.put(index.getName(), rvIndex);
            rvIndex.setName(index.getName());
        }
        rvIndex.setCode(index.getName());
        rvIndex.setUniqueIndex(index.isUnique());
        final RvIndex targetRvIndex = rvIndex;
        rvIndex.setIndexColumns(Streams.mapWithIndex(index.getColumns().stream(), (column, idx) -> {
            RvIndexColumn rvIndexColumn = new RvIndexColumn();
            rvIndexColumn.setIndex(targetRvIndex);
            rvIndexColumn.setColumn(rvColumnMap.get(column.getName()));
            rvIndexColumn.setOrderNum((int) idx);
            return rvIndexColumn;
        }).collect(Collectors.toList()));
        return rvIndex;
    }

    private RvPrimaryKey buildRvPrimaryKey(PrimaryKey primaryKey, RvPrototype rvPrototype, Map<String, RvColumn> rvColumnMap, Map<String, RvIndex> rvIndexMap) {
        RvPrimaryKey rvPrimaryKey = rvPrototype.getPrimaryKey();
        boolean isNew = rvPrimaryKey == null;
        if (isNew) {
            rvPrimaryKey = new RvPrimaryKey();
            log.info("表 " + primaryKey.getTable().getName() + " 新增主键 " + primaryKey.getName());
            rvPrimaryKey.setName(primaryKey.getName());
        }
        rvPrimaryKey.setCode(primaryKey.getName());
        if (primaryKey.getBackingIndex() != null) {
            rvPrimaryKey.setBackingIndex(rvIndexMap.get(primaryKey.getBackingIndex().getName()));
        }
        final RvPrimaryKey targetRvPrimaryKey = rvPrimaryKey;
        rvPrimaryKey.setPrimaryKeyColumns(Streams.mapWithIndex(primaryKey.getColumns().stream(), (column, idx) -> {
            RvPrimaryKeyColumn rvPrimaryKeyColumn = new RvPrimaryKeyColumn();
            rvPrimaryKeyColumn.setPrimaryKey(targetRvPrimaryKey);
            rvPrimaryKeyColumn.setColumn(rvColumnMap.get(column.getName()));
            rvPrimaryKeyColumn.setOrderNum((int) idx);
            return rvPrimaryKeyColumn;
        }).collect(Collectors.toList()));
        return rvPrimaryKey;
    }

    private RvForeignKey buildRvForeignKey(ForeignKey foreignKey, Map<String, RvPrototype> rvPrototypeMap, Map<String, RvColumn> rvColumnMap, Map<String, RvIndex> rvIndexMap, Map<String, RvForeignKey> rvForeignKeyMap) {
        RvForeignKey rvForeignKey = rvForeignKeyMap.get(foreignKey.getName());
        boolean isNew = rvForeignKey == null;
        if (isNew) {
            rvForeignKey = new RvForeignKey();
            log.info("表 " + foreignKey.getPrimaryKeyTable().getName() + " 新增外键 " + foreignKey.getName());
            rvForeignKeyMap.put(foreignKey.getName(), rvForeignKey);
            rvForeignKey.setName(foreignKey.getName());
        }
        rvForeignKey.setCode(foreignKey.getName());
        // 是否级联删除
        rvForeignKey.setCascadeDelete(importedKeyCascade.equals(foreignKey.getDeleteRule()));
        if (foreignKey.getBackingIndex() != null) {
            rvForeignKey.setBackingIndex(rvIndexMap.get(foreignKey.getBackingIndex().getName()));
        }
        final RvForeignKey targetRvForeignKey = rvForeignKey;
        rvForeignKey.setForeignKeyLocalColumns(Streams.mapWithIndex(foreignKey.getPrimaryKeyColumns().stream(), (column, idx) -> {
            RvForeignKeyLocalColumn rvForeignKeyLocalColumn = new RvForeignKeyLocalColumn();
            rvForeignKeyLocalColumn.setForeignKey(targetRvForeignKey);
            rvForeignKeyLocalColumn.setColumn(rvColumnMap.get(column.getName()));
            rvForeignKeyLocalColumn.setOrderNum((int) idx);
            return rvForeignKeyLocalColumn;
        }).collect(Collectors.toList()));
        rvForeignKey.setForeignPrototype(rvPrototypeMap.get(foreignKey.getForeignKeyTable().getName()));
        rvForeignKey.setForeignKeyForeignColumns(Streams.mapWithIndex(foreignKey.getForeignKeyColumns().stream(), (column, idx) -> {
            RvForeignKeyForeignColumn rvForeignKeyForeignColumn = new RvForeignKeyForeignColumn();
            rvForeignKeyForeignColumn.setForeignKey(targetRvForeignKey);
            rvForeignKeyForeignColumn.setColumn(rvColumnMap.get(column.getName()));
            rvForeignKeyForeignColumn.setOrderNum((int) idx);
            return rvForeignKeyForeignColumn;
        }).collect(Collectors.toList()));
        return rvForeignKey;
    }

    private RvUniqueConstraint buildRvUniqueConstraint(UniqueConstraint uniqueConstraint, Map<String, RvColumn> rvColumnMap, Map<String, RvIndex> rvIndexMap, Map<String, RvUniqueConstraint> rvUniqueConstraintMap) {
        RvUniqueConstraint rvUniqueConstraint = rvUniqueConstraintMap.get(uniqueConstraint.getName());
        boolean isNew = rvUniqueConstraint == null;
        if (isNew) {
            rvUniqueConstraint = new RvUniqueConstraint();
            log.info("表 " + uniqueConstraint.getRelation().getName() + " 新增唯一性约束 " + uniqueConstraint.getName());
            rvUniqueConstraintMap.put(uniqueConstraint.getName(), rvUniqueConstraint);
            rvUniqueConstraint.setName(uniqueConstraint.getName());
        }
        rvUniqueConstraint.setCode(uniqueConstraint.getColumnNames());
        if (uniqueConstraint.getBackingIndex() != null) {
            rvUniqueConstraint.setBackingIndex(rvIndexMap.get(uniqueConstraint.getBackingIndex().getName()));
        }
        final RvUniqueConstraint targetRvUniqueConstraint = rvUniqueConstraint;
        rvUniqueConstraint.setUniqueConstraintColumns(Streams.mapWithIndex(uniqueConstraint.getColumns().stream(), (column, idx) -> {
            RvUniqueConstraintColumn rvUniqueConstraintColumn = new RvUniqueConstraintColumn();
            rvUniqueConstraintColumn.setUniqueConstraint(targetRvUniqueConstraint);
            rvUniqueConstraintColumn.setColumn(rvColumnMap.get(column.getName()));
            rvUniqueConstraintColumn.setOrderNum((int) idx);
            return rvUniqueConstraintColumn;
        }).collect(Collectors.toList()));
        return rvUniqueConstraint;
    }

    private DiffResult getHibernateDiff() throws LiquibaseException {
        DatabaseFactory factory = DatabaseFactory.getInstance();
        Map<String, String> hibernateProperties = new HashMap<>(16);
        hibernateProperties.put("dialect", jpaProperties.getProperties().get("hibernate.dialect"));
        hibernateProperties.put("hibernate.physical_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy");
        hibernateProperties.put("hibernate.implicit_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
        // temp.use_jdbc_metadata_defaults属性用于跳过数据库连接检查，因为这里的reference，即hibernate不是真正的数据库
        hibernateProperties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        String url = buildUrl("hibernate:spring:org.laputa.rivulet", hibernateProperties);
        String driver = "liquibase.ext.hibernate.database.connection.HibernateDriver";
        Database database = factory.openDatabase(url, null, null, driver, null, null, null, null);
        Liquibase liquibase = new Liquibase((DatabaseChangeLog) null, null, database);
        DiffResult diffResult = liquibase.diff(database, null, new CompareControl());
        database.close();
        return diffResult;
    }

    private String buildUrl(String baseUrl, Map<String, String> parameterMap) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        boolean firstFlag = true;
        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            if (firstFlag) {
                firstFlag = false;
                urlBuilder.append('?');
            } else {
                urlBuilder.append('&');
            }
            urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return urlBuilder.toString();
    }
}
