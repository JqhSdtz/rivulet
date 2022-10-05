package org.laputa.rivulet.module.datamodel.service;

import com.google.common.collect.Streams;
import liquibase.Liquibase;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.exception.LiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import lombok.extern.slf4j.Slf4j;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.util.RedissonLockUtil;
import org.laputa.rivulet.module.datamodel.entity.*;
import org.laputa.rivulet.module.datamodel.entity.column_relation.*;
import org.laputa.rivulet.module.datamodel.repository.RvPrototypeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static liquibase.structure.core.ForeignKeyConstraintType.importedKeyCascade;

/**
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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("检测内部实体类变更——开始");
        DiffResult hibernateDiffResult = getHibernateDiff();
        Set<? extends DatabaseObject> hibernateObjects = hibernateDiffResult.getMissingObjects();
        List<Table> tableList = new ArrayList<>();
        hibernateObjects.forEach(databaseObject -> {
            if (databaseObject instanceof Table) {
                tableList.add((Table) databaseObject);
            }
        });
        Result result = redissonLockUtil.doWithLock("checkBuiltInDataModel", () -> {
            List<RvPrototype> prototypeList = rvPrototypeRepository.findAll();
            Map<String, RvPrototype> rvPrototypeMap = new HashMap<>(tableList.size());
            prototypeList.forEach(prototype -> rvPrototypeMap.put(prototype.getCode(), prototype));
            List<RvPrototype> targetRvPrototypeList = new ArrayList<>(tableList.size());
            // 先把所有的prototype建好，便于构造过程中的外键引用
            tableList.forEach(table -> {
                if (!rvPrototypeMap.containsKey(table.getName())) {
                    rvPrototypeMap.put(table.getName(), new RvPrototype());
                }
            });
            tableList.forEach(table -> {
                RvPrototype oriPrototype = rvPrototypeMap.get(table.getName());
                // 原来没有的prototype
                if (oriPrototype.getId() == null) {
                    targetRvPrototypeList.add(buildRvPrototype(table, rvPrototypeMap));
                }
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
        rvPrototype.setName(table.getName());
        rvPrototype.setCode(table.getName());
        rvPrototype.setRemark(table.getRemarks());
        Map<String, RvColumn> rvColumnMap = new HashMap<>(table.getColumns().size());
        rvPrototype.setColumns(table.getColumns().stream().map(column -> {
            RvColumn rvColumn = buildRvColumn(column);
            rvColumn.setPrototype(rvPrototype);
            rvColumnMap.put(rvColumn.getCode(), rvColumn);
            return rvColumn;
        }).collect(Collectors.toList()));
        Map<String, RvIndex> rvIndexMap = new HashMap<>(table.getIndexes().size());
        rvPrototype.setIndexes(Streams.mapWithIndex(table.getIndexes().stream(), (index, idx) -> {
            RvIndex rvIndex = buildRvIndex(index, rvColumnMap);
            rvIndex.setOrderNum((int) idx);
            rvIndex.setPrototype(rvPrototype);
            rvIndexMap.put(rvIndex.getCode(), rvIndex);
            return rvIndex;
        }).collect(Collectors.toList()));
        rvPrototype.setPrimaryKey(buildRvPrimaryKey(table.getPrimaryKey(), rvColumnMap, rvIndexMap));
        rvPrototype.setForeignKeys(Streams.mapWithIndex(table.getOutgoingForeignKeys().stream(), (foreignKey, idx) -> {
            RvForeignKey rvForeignKey = buildRvForeignKey(foreignKey, rvPrototypeMap, rvColumnMap, rvIndexMap);
            rvForeignKey.setOrderNum((int) idx);
            rvForeignKey.setPrototype(rvPrototype);
            return rvForeignKey;
        }).collect(Collectors.toList()));
        rvPrototype.setUniqueConstraints(Streams.mapWithIndex(table.getUniqueConstraints().stream(), (uniqueConstraint, idx) -> {
            RvUniqueConstraint rvUniqueConstraint = buildRvUniqueConstraint(uniqueConstraint, rvColumnMap, rvIndexMap);
            rvUniqueConstraint.setOrderNum((int) idx);
            rvUniqueConstraint.setPrototype(rvPrototype);
            return rvUniqueConstraint;
        }).collect(Collectors.toList()));
        rvPrototype.setDbSyncFlag(true);
        return rvPrototype;
    }

    private RvColumn buildRvColumn(Column column) {
        RvColumn rvColumn = new RvColumn();
        rvColumn.setName(column.getName());
        rvColumn.setCode(column.getName());
        rvColumn.setDataType(column.getType().toString());
        rvColumn.setIsNullable(column.isNullable());
        rvColumn.setOrderNum(column.getOrder());
        if (column.getDefaultValue() != null) {
            rvColumn.setDefaultValue(column.getDefaultValue().toString());
        }
        rvColumn.setRemark(column.getRemarks());
        return rvColumn;
    }

    private RvIndex buildRvIndex(Index index, Map<String, RvColumn> rvColumnMap) {
        RvIndex rvIndex = new RvIndex();
        rvIndex.setName(index.getName());
        rvIndex.setCode(index.getName());
        rvIndex.setUniqueIndex(index.isUnique());
        rvIndex.setIndexColumns(Streams.mapWithIndex(index.getColumns().stream(), (column, idx) -> {
            RvIndexColumn rvIndexColumn = new RvIndexColumn();
            rvIndexColumn.setIndex(rvIndex);
            rvIndexColumn.setColumn(rvColumnMap.get(column.getName()));
            rvIndexColumn.setOrderNum((int) idx);
            return rvIndexColumn;
        }).collect(Collectors.toList()));
        return rvIndex;
    }

    private RvPrimaryKey buildRvPrimaryKey(PrimaryKey primaryKey, Map<String, RvColumn> rvColumnMap, Map<String, RvIndex> rvIndexMap) {
        RvPrimaryKey rvPrimaryKey = new RvPrimaryKey();
        rvPrimaryKey.setName(primaryKey.getName());
        rvPrimaryKey.setCode(primaryKey.getName());
        if (primaryKey.getBackingIndex() != null) {
            rvPrimaryKey.setBackingIndex(rvIndexMap.get(primaryKey.getBackingIndex().getName()));
        }
        rvPrimaryKey.setPrimaryKeyColumns(Streams.mapWithIndex(primaryKey.getColumns().stream(), (column, idx) -> {
            RvPrimaryKeyColumn rvPrimaryKeyColumn = new RvPrimaryKeyColumn();
            rvPrimaryKeyColumn.setPrimaryKey(rvPrimaryKey);
            rvPrimaryKeyColumn.setColumn(rvColumnMap.get(column.getName()));
            rvPrimaryKeyColumn.setOrderNum((int) idx);
            return rvPrimaryKeyColumn;
        }).collect(Collectors.toList()));
        return rvPrimaryKey;
    }

    private RvForeignKey buildRvForeignKey(ForeignKey foreignKey, Map<String, RvPrototype> rvPrototypeMap, Map<String, RvColumn> rvColumnMap, Map<String, RvIndex> rvIndexMap) {
        RvForeignKey rvForeignKey = new RvForeignKey();
        rvForeignKey.setName(foreignKey.getName());
        rvForeignKey.setCode(foreignKey.getName());
        // 是否级联删除
        rvForeignKey.setCascadeDelete(importedKeyCascade.equals(foreignKey.getDeleteRule()));
        if (foreignKey.getBackingIndex() != null) {
            rvForeignKey.setBackingIndex(rvIndexMap.get(foreignKey.getBackingIndex().getName()));
        }
        rvForeignKey.setForeignKeyLocalColumns(Streams.mapWithIndex(foreignKey.getPrimaryKeyColumns().stream(), (column, idx) -> {
            RvForeignKeyLocalColumn rvForeignKeyLocalColumn = new RvForeignKeyLocalColumn();
            rvForeignKeyLocalColumn.setForeignKey(rvForeignKey);
            rvForeignKeyLocalColumn.setColumn(rvColumnMap.get(column.getName()));
            rvForeignKeyLocalColumn.setOrderNum((int) idx);
            return rvForeignKeyLocalColumn;
        }).collect(Collectors.toList()));
        rvForeignKey.setForeignPrototype(rvPrototypeMap.get(foreignKey.getForeignKeyTable().getName()));
        rvForeignKey.setForeignKeyForeignColumns(Streams.mapWithIndex(foreignKey.getForeignKeyColumns().stream(), (column, idx) -> {
            RvForeignKeyForeignColumn rvForeignKeyForeignColumn = new RvForeignKeyForeignColumn();
            rvForeignKeyForeignColumn.setForeignKey(rvForeignKey);
            rvForeignKeyForeignColumn.setColumn(rvColumnMap.get(column.getName()));
            rvForeignKeyForeignColumn.setOrderNum((int) idx);
            return rvForeignKeyForeignColumn;
        }).collect(Collectors.toList()));
        return rvForeignKey;
    }

    private RvUniqueConstraint buildRvUniqueConstraint(UniqueConstraint uniqueConstraint, Map<String, RvColumn> rvColumnMap, Map<String, RvIndex> rvIndexMap) {
        RvUniqueConstraint rvUniqueConstraint = new RvUniqueConstraint();
        rvUniqueConstraint.setName(uniqueConstraint.getName());
        rvUniqueConstraint.setCode(uniqueConstraint.getColumnNames());
        if (uniqueConstraint.getBackingIndex() != null) {
            rvUniqueConstraint.setBackingIndex(rvIndexMap.get(uniqueConstraint.getBackingIndex().getName()));
        }
        rvUniqueConstraint.setUniqueConstraintColumns(Streams.mapWithIndex(uniqueConstraint.getColumns().stream(), (column, idx) -> {
            RvUniqueConstraintColumn rvUniqueConstraintColumn = new RvUniqueConstraintColumn();
            rvUniqueConstraintColumn.setUniqueConstraint(rvUniqueConstraint);
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
