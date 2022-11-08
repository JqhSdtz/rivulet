package org.laputa.rivulet.module.data_model.service;

import com.google.common.collect.Streams;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.LiquibaseException;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.internal.MetadataImpl;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.state.AppState;
import org.laputa.rivulet.common.util.RedissonLockUtil;
import org.laputa.rivulet.common.util.TerminalKeyUtil;
import org.laputa.rivulet.common.util.TimeUnitUtil;
import org.laputa.rivulet.module.app.property.TerminalKeyProperty;
import org.laputa.rivulet.module.app.service.GitService;
import org.laputa.rivulet.module.data_model.entity.*;
import org.laputa.rivulet.module.data_model.entity.column_relation.*;
import org.laputa.rivulet.module.data_model.model.RemarkMetaInfo;
import org.laputa.rivulet.module.data_model.repository.*;
import org.laputa.rivulet.module.data_model.util.RemarkMetaInfoUtil;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static liquibase.structure.core.ForeignKeyConstraintType.importedKeyCascade;

/**
 * 系统内部的数据模型自动同步到数据模型对应表中
 * 需要注意的是，系统内部的表、字段等数据模型改名字后无法和之前的关联，即之前对应的数据都将丢失
 * 所以系统内部表和字段一般情况下禁止改名字
 * <p>
 * order不设置默认是整型最大值，默认最后运行
 *
 * @author JQH
 * @since 下午 6:47 22/09/04
 */
@Service
@Order()
@Slf4j
public class BuiltInDataModelService implements ApplicationRunner {
    @Resource
    private JpaProperties jpaProperties;
    @Resource
    private AppState appState;
    @Resource
    private DataSource dataSource;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RedissonLockUtil redissonLockUtil;
    @Resource
    private GitService gitService;
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
    @Resource
    private TerminalKeyUtil terminalKeyUtil;
    @Resource
    private TerminalKeyProperty terminalKeyProperty;

    private RBucket<String> confirmKeyBucket;
    private Map<String, Field> columnFieldMap = new HashMap<>();
    private DiffResult diffResult;
    private String currentStructureUpdateSql;
    private DatabaseChangeLog currentDatabaseChangeLog;

    @PostConstruct
    private void postConstruct() {
        confirmKeyBucket = redissonClient.getBucket("confirmStructureUpdateSqlKey");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) throws Exception {
        refreshStructureUpdateSql();
        if (!appState.isBuiltInDataModelSynced()) {
            log.info("检测到系统内部数据模型有更新，请访问系统以确认更新SQL");
            String timeStr = TimeUnitUtil.format(terminalKeyProperty.getTimeout(), terminalKeyProperty.getTimeUnit());
            log.info("确认更新的密钥为: {}，请在{}内进行确认操作", terminalKeyUtil.generateTerminalKey(confirmKeyBucket), timeStr);
            appState.registerStateChangeCallback("builtInDataModelSynced", state -> {
                if (state.getCurrentValue().equals(false)) return;
                syncBuiltInDataModel();
                log.info("内部数据模型更新完毕");
            });
        }
    }

    public String getCurrentStructureUpdateSql() {
        return currentStructureUpdateSql;
    }

    @SneakyThrows
    public Result<Void> confirmUpdateStructureSql(String key) {
        String confirmKey = confirmKeyBucket.get();
        if (confirmKey == null) {
            return Result.fail("ConfirmKeyExpired", "确认密钥已过期，请重启服务");
        } else if (!confirmKey.equals(key)) {
            return Result.fail("WrongConfirmKey", "确认密钥不正确");
        }
        doStructureUpdate();
        return Result.succeedWithMessage("内部数据模型更新成功");
    }

    public synchronized void refreshStructureUpdateSql() throws LiquibaseException, SQLException {
        DatabaseFactory factory = DatabaseFactory.getInstance();
        Map<String, String> hibernateProperties = new HashMap<>();
        hibernateProperties.put("dialect", jpaProperties.getProperties().get("hibernate.dialect"));
        hibernateProperties.put("hibernate.physical_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy");
        hibernateProperties.put("hibernate.implicit_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
        // temp.use_jdbc_metadata_defaults属性用于跳过数据库连接检查，因为这里的reference，即hibernate不是真正的数据库
        hibernateProperties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        // 禁止liquibase-hibernate显示found column/index/...的信息，太多了
        hibernateProperties.put("liquibase.show_found_info", "false");
        String url = buildUrl("hibernate:spring:org.laputa.rivulet", hibernateProperties);
        String driver = "liquibase.ext.hibernate.database.connection.HibernateDriver";
        HibernateDatabase database = (HibernateDatabase) factory.openDatabase(url, null, null, driver, null, null, null, null);
        processMetadata((MetadataImpl) database.getMetadata());
        final DatabaseChangeLog databaseChangeLog = new DatabaseChangeLog();
        Connection connection = this.dataSource.getConnection();
        Database targetDataBase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase(databaseChangeLog, null, targetDataBase);
        this.diffResult = liquibase.diff(database, targetDataBase, new CompareControl());
        filterDiffResult();
        // hibernate的database的schema是'HIBERNATE'，这里要忽略schema才能正确执行update
        DiffOutputControl diffOutputControl = new DiffOutputControl(false, false, false, null);
        DiffToChangeLog diffToChangeLog = new DiffToChangeLog(this.diffResult, diffOutputControl);
        List<ChangeSet> changeSets = diffToChangeLog.generateChangeSets();
        changeSets.forEach(changeSet -> {
            // 加上这句可以防止因为update操作抛异常引起回滚而意外地将update操作应用到数据库上，具体源码怎么做的我也不知道
            changeSet.getRollback().getChanges().clear();
            changeSet.setFilePath(changeSet.getId());
            databaseChangeLog.addChangeSet(changeSet);
        });
        this.currentDatabaseChangeLog = databaseChangeLog;
        if (changeSets.size() == 0) {
            this.appState.setBuiltInDataModelSynced(true);
        }
        StringWriter stringWriter = new StringWriter();
        // 指定writer后就不会真正执行update，而是将sql语句输入writer中
        liquibase.update(new Contexts(), stringWriter);
        targetDataBase.close();
        database.close();
        this.currentStructureUpdateSql = stringWriter.toString();
    }

    private void doStructureUpdate() throws SQLException, LiquibaseException {
        Connection connection = this.dataSource.getConnection();
        Database targetDataBase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase(this.currentDatabaseChangeLog, null, targetDataBase);
        liquibase.update(new Contexts());
        targetDataBase.close();
    }

    /**
     * 从待删除的数据库对象中过滤掉目标数据库中非系统内部的数据模型
     */
    private void filterDiffResult() {
        Iterator<? extends DatabaseObject> iterator = this.diffResult.getUnexpectedObjects().iterator();
        while (iterator.hasNext()) {
            DatabaseObject object = iterator.next();
            String tableRemark = null;
            if (object instanceof Table) {
                tableRemark = ((Table) object).getRemarks();
            } else if (object instanceof Column) {
                tableRemark = ((Column) object).getRelation().getRemarks();
            } else if (object instanceof ForeignKey) {
                tableRemark = ((ForeignKey) object).getPrimaryKeyTable().getRemarks();
            } else if (object instanceof Index) {
                tableRemark = ((Index) object).getRelation().getRemarks();
            } else if (object instanceof PrimaryKey) {
                tableRemark = ((PrimaryKey) object).getTable().getRemarks();
            } else if (object instanceof UniqueConstraint) {
                tableRemark = ((UniqueConstraint) object).getRelation().getRemarks();
            }
            final String finalTableName = tableRemark;
            boolean isBuiltIn;
            // 如果该DatabaseObject不属于上述类型，则不属于系统内置数据模型
            if (finalTableName == null) {
                isBuiltIn = false;
            } else {
                RemarkMetaInfo metaInfo = RemarkMetaInfoUtil.getMetaInfo(tableRemark);
                isBuiltIn = metaInfo.isBuiltIn();
            }
            if (!isBuiltIn) {
                iterator.remove();
            }
        }
    }

    @SneakyThrows
    private void syncBuiltInDataModel() {
        Result result = redissonLockUtil.doWithLock("checkBuiltInDataModel", () -> doSyncBuiltInDataModel());
        if (!result.isSuccessful()) {
            throw result.toRawException();
        }
    }

    @SneakyThrows
    private Result doSyncBuiltInDataModel() {
        DatabaseSnapshot hibernateSnapshot = this.diffResult.getReferenceSnapshot();
        DatabaseObjectCollection hibernateDatabaseObjects = (DatabaseObjectCollection) hibernateSnapshot.getSerializableFieldValue("objects");
        Map<Class<? extends DatabaseObject>, Set<? extends DatabaseObject>> objectMapByClass = hibernateDatabaseObjects.toMap();
        Set<Table> tableSet = (Set<Table>) objectMapByClass.get(Table.class);
        List<RvPrototype> originalPrototypeList = rvPrototypeRepository.findAll();
        List<RvPrototype> toDeletePrototypeList = new ArrayList<>();
        Map<String, RvPrototype> rvPrototypeMap = new HashMap<>(tableSet.size());
        originalPrototypeList.forEach(prototype -> {
            if (tableSet.stream().filter(table -> table.getName().equals(prototype.getCode())).findAny().isPresent()) {
                rvPrototypeMap.put(prototype.getCode(), prototype);
            } else {
                toDeletePrototypeList.add(prototype);
            }
        });
        rvPrototypeRepository.deleteAllById(toDeletePrototypeList.stream().map(rvPrototype -> rvPrototype.getId()).collect(Collectors.toList()));
        gitService.removeBuiltInRvPrototypes(toDeletePrototypeList);
        List<RvPrototype> toSaveRvPrototypeList = new ArrayList<>(tableSet.size());
        // 先把所有的prototype建好，便于构造过程中的外键引用
        tableSet.forEach(table -> {
            if (!rvPrototypeMap.containsKey(table.getName())) {
                rvPrototypeMap.put(table.getName(), new RvPrototype());
            }
        });
        tableSet.forEach(table -> toSaveRvPrototypeList.add(buildRvPrototype(table, rvPrototypeMap)));
        // 每次启动都全量覆盖保存内部数据模型
        rvPrototypeRepository.saveAll(toSaveRvPrototypeList);
        gitService.addBuiltInRvPrototypes(toSaveRvPrototypeList);
        return Result.succeed();
    }

    private RvPrototype buildRvPrototype(Table table, Map<String, RvPrototype> rvPrototypeMap) {
        RvPrototype rvPrototype = rvPrototypeMap.get(table.getName());
        boolean isNew = rvPrototype.getId() == null;
        rvPrototype.setCode(table.getName());
        rvPrototype.setBuiltIn(true);
        if (isNew) {
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
        rvPrototype.setSyncFlag(true);
        return rvPrototype;
    }

    private RvColumn buildRvColumn(Column column, Map<String, RvColumn> rvColumnMap) {
        RvColumn rvColumn = rvColumnMap.get(column.getName());
        boolean isNew = rvColumn == null;
        if (isNew) {
            rvColumn = new RvColumn();
            rvColumnMap.put(column.getName(), rvColumn);
            rvColumn.setName(column.getName());
        }
        rvColumn.setCode(column.getName());
        rvColumn.setDataType(column.getType().toString());
        rvColumn.setDefaultValue(String.valueOf(column.getDefaultValue()));
        rvColumn.setNullable(column.isNullable());
        rvColumn.setAutoIncrement(column.isAutoIncrement());
        if (column.isAutoIncrement()) {
            rvColumn.setIncrementBy(column.getAutoIncrementInformation().getIncrementBy());
            rvColumn.setStartWith(column.getAutoIncrementInformation().getStartWith());
        }
        if (rvColumn.getOrderNum() == null) {
            rvColumn.setOrderNum(column.getOrder());
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

    private void processMetadata(MetadataImpl metadata) {
        Map<String, PersistentClass> entityBindingMap = metadata.getEntityBindingMap();
        // columnFieldMap只需要初始化一次即可
        if (this.columnFieldMap.isEmpty()) {
            entityBindingMap.forEach((className, entity) -> {
                Class entityClass = entity.getMappedClass();
                for (Field field : entityClass.getFields()) {
                    Property property = entity.getProperty(field.getName());
                    String tableName = entity.getTable().getName();
                    String columnName = property.getValue().getColumnIterator().next().getText();
                    this.columnFieldMap.put(tableName + "." + columnName, field);
                }
            });
        }
        entityBindingMap.forEach((className, entity) -> {
            String oriComment = entity.getTable().getComment();
            RemarkMetaInfo metaInfo = RemarkMetaInfoUtil.getMetaInfo(oriComment);
            metaInfo.setBuiltIn(true);
            String newComment = RemarkMetaInfoUtil.setMetaInfo(oriComment, metaInfo);
            entity.getTable().setComment(newComment);
        });
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
