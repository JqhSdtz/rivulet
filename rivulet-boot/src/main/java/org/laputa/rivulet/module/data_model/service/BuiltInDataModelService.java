package org.laputa.rivulet.module.data_model.service;

import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Streams;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.Scope;
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
import liquibase.ext.hibernate.DatabaseObjectAttrName;
import liquibase.ext.hibernate.database.HibernateDatabase;
import liquibase.ext.hibernate.util.TableRemarkMetaInfo;
import liquibase.ext.hibernate.util.TableRemarkMetaInfoUtil;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.statement.NotNullConstraint;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.*;
import lombok.Getter;
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
import org.laputa.rivulet.module.data_model.entity.RvColumn;
import org.laputa.rivulet.module.data_model.entity.RvIndex;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.laputa.rivulet.module.data_model.entity.column_relation.*;
import org.laputa.rivulet.module.data_model.entity.constraint.RvForeignKey;
import org.laputa.rivulet.module.data_model.entity.constraint.RvNotNull;
import org.laputa.rivulet.module.data_model.entity.constraint.RvPrimaryKey;
import org.laputa.rivulet.module.data_model.entity.constraint.RvUnique;
import org.laputa.rivulet.module.data_model.entity.inter.DataModelEntityInterface;
import org.laputa.rivulet.module.data_model.repository.*;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private RvUniqueRepository rvUniqueRepository;
    @Resource
    private RvNotNullRepository rvNotNullRepository;
    @Resource
    private TerminalKeyUtil terminalKeyUtil;
    @Resource
    private TerminalKeyProperty terminalKeyProperty;
    @Resource
    private TransactionTemplate transactionTemplate;

    private RBucket<String> confirmKeyBucket;
    private final Map<String, Field> columnFieldMap = new HashMap<>();
    private DiffResult diffResult;
    @Getter
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

    public synchronized void refreshStructureUpdateSql() throws Exception {
        DatabaseFactory factory = DatabaseFactory.getInstance();
        Map<String, String> hibernateProperties = new HashMap<>();
        hibernateProperties.put("dialect", jpaProperties.getProperties().get("hibernate.dialect"));
        // springboot 3.4.2版本中没有SpringPhysicalNamingStrategy 参考https://github.com/openrewrite/rewrite-spring/issues/339
        hibernateProperties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        hibernateProperties.put("hibernate.implicit_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy");
        // boot.allow_jdbc_metadata_access属性用于跳过数据库连接检查，因为这里的reference，即hibernate不是真正的数据库
        hibernateProperties.put("hibernate.boot.allow_jdbc_metadata_access", "false");
        // 禁止liquibase-hibernate显示found column/index/...的信息，太多了
        hibernateProperties.put("liquibase.show_found_info", "false");
        hibernateProperties.put("liquibase.show_converted_info", "false");
        String url = buildUrl("hibernate:spring:org.laputa.rivulet", hibernateProperties);
        String driver = "liquibase.ext.hibernate.database.connection.HibernateDriver";
        HibernateDatabase database = (HibernateDatabase) factory.openDatabase(url, null, null, driver, null, null, null, null);
        processMetadata((MetadataImpl) database.getMetadata());
        final DatabaseChangeLog databaseChangeLog = new DatabaseChangeLog();
        // logicalFilePath设置为随机字符，防止受过去的changeLog影响
        String changeLogFilePath = RandomUtil.randomString(64);
        databaseChangeLog.setLogicalFilePath(changeLogFilePath);
        databaseChangeLog.setPhysicalFilePath(changeLogFilePath);
        Connection connection = this.dataSource.getConnection();
        Database targetDataBase = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        Liquibase liquibase = new Liquibase(databaseChangeLog, null, targetDataBase);
        this.diffResult = liquibase.diff(database, targetDataBase, CompareControl.STANDARD);
        filterDiffResult();
        // hibernate的database的schema是'HIBERNATE'，这里要忽略schema才能正确执行update
        DiffOutputControl diffOutputControl = new DiffOutputControl(false, false, false, null);
        DiffToChangeLog diffToChangeLog = new DiffToChangeLog(this.diffResult, diffOutputControl);
        List<ChangeSet> changeSets = diffToChangeLog.generateChangeSets();
        changeSets.forEach(changeSet -> {
            // 加上这句可以防止因为update操作抛异常引起回滚而意外地将update操作应用到数据库上，具体源码怎么做的我也不知道
            changeSet.getRollback().getChanges().clear();
            changeSet.setFilePath(changeSet.getId());
            // 因为没法直接设置changeLog，而不设置changeLog就会报错
            ChangeSet newChangeSet = new ChangeSet(changeSet.getId(), changeSet.getAuthor(), changeSet.isAlwaysRun(), changeSet.isRunOnChange(), changeSet.getFilePath(),
                    changeSet.getContextFilter().getOriginalString(), changeSet.getDbmsOriginalString(), changeSet.getRunWith(), changeSet.getRunWithSpoolFile(),
                    changeSet.isRunInTransaction(), changeSet.getObjectQuotingStrategy(), databaseChangeLog);
            changeSet.getChanges().forEach(newChangeSet::addChange);
            databaseChangeLog.addChangeSet(newChangeSet);
        });
        this.currentDatabaseChangeLog = databaseChangeLog;
        if (changeSets.isEmpty()) {
            this.appState.setBuiltInDataModelSynced(true);
        }
        StringWriter stringWriter = new StringWriter();
        // 指定writer后就不会真正执行update，而是将sql语句输入writer中
        // 临时调整日志等级，避免在执行过程中输出changeSet应用成功的误导信息
        // 因为changeSet只是被转成了sql，并没有执行
        Scope.child(new HashMap<>(), () -> {
            liquibase.update(new Contexts(), stringWriter);
        });
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
     * 因为根据系统内部的数据模型删除数据库的字段操作只能是针对标记为系统内置的表、字段或索引
     */
    private void filterDiffResult() {
        Iterator<? extends DatabaseObject> iterator = this.diffResult.getUnexpectedObjects().iterator();
        while (iterator.hasNext()) {
            DatabaseObject object = iterator.next();
            String remark = null;
            if (object instanceof Table) {
                remark = ((Table) object).getRemarks();
            } else if (object instanceof Column) {
                remark = ((Column) object).getRelation().getRemarks();
            } else if (object instanceof ForeignKey) {
                remark = ((ForeignKey) object).getPrimaryKeyTable().getRemarks();
            } else if (object instanceof Index) {
                remark = ((Index) object).getRelation().getRemarks();
            } else if (object instanceof PrimaryKey) {
                remark = ((PrimaryKey) object).getTable().getRemarks();
            } else if (object instanceof UniqueConstraint) {
                remark = ((UniqueConstraint) object).getRelation().getRemarks();
            }
            final String finalRemark = remark;
            boolean isBuiltIn;
            // 如果该DatabaseObject不属于上述类型，则不属于系统内置数据模型
            if (finalRemark == null) {
                isBuiltIn = false;
            } else {
                TableRemarkMetaInfo metaInfo = TableRemarkMetaInfoUtil.getMetaInfo(remark);
                isBuiltIn = metaInfo.isBuiltIn();
            }
            if (!isBuiltIn) {
                iterator.remove();
            }
        }
    }

    @SneakyThrows
    private void syncBuiltInDataModel() {
        Result result = redissonLockUtil.doWithLock("checkBuiltInDataModel", () -> doSyncBuiltInDataModelWithTransaction());
        if (!result.isSuccessful()) {
            throw result.toRawException();
        }
    }

    /**
     * 因为在执行过程中会触发hibernate的懒加载，而hibernate的懒加载需要在事务中
     *
     * @return
     */
    private Result doSyncBuiltInDataModelWithTransaction() {
        return transactionTemplate.execute(transactionStatus -> {
            try {
                return doSyncBuiltInDataModel();
            } catch (Exception exception) {
                exception.printStackTrace();
                transactionStatus.setRollbackOnly();
                return Result.fail(Result.UNEXPECTED_ERROR, "服务异常，请重试");
            }
        });
    }

    private Result doSyncBuiltInDataModel() {
        DatabaseSnapshot hibernateSnapshot = this.diffResult.getReferenceSnapshot();
        DatabaseObjectCollection hibernateDatabaseObjects = (DatabaseObjectCollection) hibernateSnapshot.getSerializableFieldValue("objects");
        Map<Class<? extends DatabaseObject>, Set<? extends DatabaseObject>> objectMapByClass = hibernateDatabaseObjects.toMap();
        Set<Table> tableSet = (Set<Table>) objectMapByClass.get(Table.class);
        List<RvPrototype> originalPrototypeList = rvPrototypeRepository.findAll();
        List<RvPrototype> toDeletePrototypeList = new ArrayList<>();
        Map<String, RvPrototype> rvPrototypeMap = new HashMap<>(tableSet.size());
        originalPrototypeList.forEach(prototype -> {
            if (tableSet.stream().anyMatch(table -> table.getName().equals(prototype.getCode()))) {
                rvPrototypeMap.put(prototype.getCode(), prototype);
            } else {
                toDeletePrototypeList.add(prototype);
            }
        });
        rvPrototypeRepository.deleteAllById(toDeletePrototypeList.stream().map(RvPrototype::getId).collect(Collectors.toList()));
        gitService.removeBuiltInRvPrototypes(toDeletePrototypeList);
        List<RvPrototype> toSaveRvPrototypeList = new ArrayList<>(tableSet.size());
        // 先把所有的prototype建好，便于构造过程中的外键引用
        tableSet.forEach(table -> {
            if (!rvPrototypeMap.containsKey(table.getName())) {
                rvPrototypeMap.put(table.getName(), new RvPrototype());
            }
        });
        AtomicInteger index = new AtomicInteger();
        tableSet.forEach(table -> toSaveRvPrototypeList.add(buildRvPrototype(table, index.getAndIncrement(), rvPrototypeMap)));
        // 每次启动都全量覆盖保存内部数据模型
        rvPrototypeRepository.saveAll(toSaveRvPrototypeList);
        gitService.addBuiltInRvPrototypes(toSaveRvPrototypeList);
        return Result.succeed();
    }

    private void setCodeAndTitleAndBuiltIn(DataModelEntityInterface dataModelEntity, DatabaseObject databaseObject) {
        dataModelEntity.setCode(databaseObject.getName());
        if (dataModelEntity.getTitle() == null) {
            dataModelEntity.setTitle(databaseObject.getAttribute(DatabaseObjectAttrName.Title, String.class));
        }
        if (dataModelEntity.getTitle() == null) {
            dataModelEntity.setTitle(databaseObject.getName());
        }
        dataModelEntity.setBuiltIn(true);
    }

    private RvPrototype buildRvPrototype(Table table, int tableIndex, Map<String, RvPrototype> rvPrototypeMap) {
        RvPrototype rvPrototype = rvPrototypeMap.get(table.getName());
        setCodeAndTitleAndBuiltIn(rvPrototype, table);
        rvPrototype.setOrderNum(tableIndex);
        if (rvPrototype.getRemark() == null) {
            rvPrototype.setRemark(table.getRemarks());
        }

        Map<String, RvColumn> rvColumnMap = new HashMap<>(table.getColumns().size());
        List<String> deletedColumnIdList = new ArrayList<>();
        if (rvPrototype.getColumns() != null) {
            rvPrototype.getColumns().forEach(rvColumn -> {
                if (table.getColumns().stream().anyMatch(column -> column.getName().equals(rvColumn.getCode()))) {
                    rvColumnMap.put(rvColumn.getCode(), rvColumn);
                } else if (rvColumn.getBuiltIn()) {
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
                if (table.getIndexes().stream().anyMatch(index -> index.getName().equals(rvIndex.getCode()))) {
                    rvIndexMap.put(rvIndex.getCode(), rvIndex);
                } else if (rvIndex.getBuiltIn()) {
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
                if (table.getOutgoingForeignKeys().stream().anyMatch(foreignKey -> foreignKey.getName().equals(rvForeignKey.getCode()))) {
                    rvForeignKeyMap.put(rvForeignKey.getCode(), rvForeignKey);
                } else if (rvForeignKey.getBuiltIn()) {
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

        Map<String, RvUnique> rvUniqueMap = new HashMap<>(table.getUniqueConstraints().size());
        List<String> deletedUniqueIdList = new ArrayList<>();
        if (rvPrototype.getUniques() != null) {
            rvPrototype.getUniques().forEach(rvUnique -> {
                if (table.getUniqueConstraints().stream().anyMatch(uniqueConstraint -> uniqueConstraint.getName().equals(rvUnique.getCode()))) {
                    rvUniqueMap.put(rvUnique.getCode(), rvUnique);
                } else if (rvUnique.getBuiltIn()) {
                    deletedUniqueIdList.add(rvUnique.getId());
                }
            });
        }
        rvUniqueRepository.deleteAllById(deletedUniqueIdList);
        rvPrototype.setUniques(Streams.mapWithIndex(table.getUniqueConstraints().stream(), (uniqueConstraint, idx) -> {
            RvUnique rvUnique = buildRvUnique(uniqueConstraint, rvColumnMap, rvIndexMap, rvUniqueMap);
            rvUnique.setOrderNum((int) idx);
            rvUnique.setPrototype(rvPrototype);
            return rvUnique;
        }).collect(Collectors.toList()));

        Map<String, RvNotNull> rvNotNullMap = new HashMap<>(table.getNotNullConstraints().size());
        List<String> deletedNotNullIdList = new ArrayList<>();
        if (rvPrototype.getNotNulls() != null) {
            rvPrototype.getNotNulls().forEach(rvNotNull -> {
                if (table.getNotNullConstraints().stream().anyMatch(notNullConstraint -> notNullConstraint.getConstraintName().equals(rvNotNull.getCode()))) {
                    rvNotNullMap.put(rvNotNull.getCode(), rvNotNull);
                } else if (rvNotNull.getBuiltIn()) {
                    deletedNotNullIdList.add(rvNotNull.getId());
                }
            });
        }
        rvNotNullRepository.deleteAllById(deletedNotNullIdList);
        rvPrototype.setNotNulls(Streams.mapWithIndex(table.getNotNullConstraints().stream(), (notNullConstraint, idx) -> {
            RvNotNull rvNotNull = buildRvNotNull(notNullConstraint, rvColumnMap, rvNotNullMap);
            rvNotNull.setOrderNum((int) idx);
            rvNotNull.setPrototype(rvPrototype);
            return rvNotNull;
        }).collect(Collectors.toList()));

        rvPrototype.setSyncFlag(true);
        return rvPrototype;
    }

    private RvColumn buildRvColumn(Column column, Map<String, RvColumn> rvColumnMap) {
        RvColumn rvColumn = rvColumnMap.get(column.getName());
        if (rvColumn == null) {
            rvColumn = new RvColumn();
            rvColumnMap.put(column.getName(), rvColumn);
        }
        setCodeAndTitleAndBuiltIn(rvColumn, column);
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
        if (rvColumn.getDescending() == null) {
            rvColumn.setDescending(column.getDescending());
        }
        if (rvColumn.getRemark() == null) {
            rvColumn.setRemark(column.getRemarks());
        }
        return rvColumn;
    }

    private RvIndex buildRvIndex(Index index, Map<String, RvColumn> rvColumnMap, Map<String, RvIndex> rvIndexMap) {
        RvIndex rvIndex = rvIndexMap.get(index.getName());
        boolean isNew = rvIndex == null;
        if (rvIndex == null) {
            rvIndex = new RvIndex();
            rvIndexMap.put(index.getName(), rvIndex);
        }
        setCodeAndTitleAndBuiltIn(rvIndex, index);
        rvIndex.setUniqueIndex(index.isUnique());
        final RvIndex targetRvIndex = rvIndex;
        rvIndex.setIndexColumns(Streams.mapWithIndex(index.getColumns().stream(), (column, idx) -> {
            // 只有通过hibernate查出来的对象才会被代理，调用getIndexColumns方法的时候才会懒加载
            // new出来的对象没有被代理，调用getIndexColumns的时候会直接返回null
            List<RvIndexColumn> matchedRvIndexColumns = isNew ? null : targetRvIndex.getIndexColumns().stream()
                    .filter(indexColumn -> column.getName().equals(indexColumn.getColumn().getCode()))
                    .toList();
            RvIndexColumn rvIndexColumn;
            if (matchedRvIndexColumns != null && !matchedRvIndexColumns.isEmpty()) {
                rvIndexColumn = matchedRvIndexColumns.get(0);
            } else {
                rvIndexColumn = new RvIndexColumn();
                rvIndexColumn.setBuiltIn(true);
                rvIndexColumn.setIndex(targetRvIndex);
                rvIndexColumn.setColumn(rvColumnMap.get(column.getName()));
                rvIndexColumn.setOrderNum((int) idx);
            }
            return rvIndexColumn;
        }).collect(Collectors.toList()));
        return rvIndex;
    }

    private RvPrimaryKey buildRvPrimaryKey(PrimaryKey primaryKey, RvPrototype rvPrototype, Map<String, RvColumn> rvColumnMap, Map<String, RvIndex> rvIndexMap) {
        RvPrimaryKey rvPrimaryKey = rvPrototype.getPrimaryKey();
        boolean isNew = rvPrimaryKey == null;
        if (rvPrimaryKey == null) {
            rvPrimaryKey = new RvPrimaryKey();
        }
        setCodeAndTitleAndBuiltIn(rvPrimaryKey, primaryKey);
        if (primaryKey.getBackingIndex() != null) {
            rvPrimaryKey.setBackingIndex(rvIndexMap.get(primaryKey.getBackingIndex().getName()));
        }
        final RvPrimaryKey targetRvPrimaryKey = rvPrimaryKey;
        rvPrimaryKey.setPrimaryKeyColumns(Streams.mapWithIndex(primaryKey.getColumns().stream(), (column, idx) -> {
            List<RvPrimaryKeyColumn> matchedRvPrimaryKeyColumns = isNew ? null : targetRvPrimaryKey.getPrimaryKeyColumns().stream()
                    .filter(primaryKeyColumn -> column.getName().equals(primaryKeyColumn.getColumn().getCode()))
                    .toList();
            RvPrimaryKeyColumn rvPrimaryKeyColumn;
            if (matchedRvPrimaryKeyColumns != null && !matchedRvPrimaryKeyColumns.isEmpty()) {
                rvPrimaryKeyColumn = matchedRvPrimaryKeyColumns.get(0);
            } else {
                rvPrimaryKeyColumn = new RvPrimaryKeyColumn();
                rvPrimaryKeyColumn.setBuiltIn(true);
                rvPrimaryKeyColumn.setPrimaryKey(targetRvPrimaryKey);
                rvPrimaryKeyColumn.setColumn(rvColumnMap.get(column.getName()));
                rvPrimaryKeyColumn.setOrderNum((int) idx);
            }
            return rvPrimaryKeyColumn;
        }).collect(Collectors.toList()));
        return rvPrimaryKey;
    }

    private RvForeignKey buildRvForeignKey(ForeignKey foreignKey, Map<String, RvPrototype> rvPrototypeMap, Map<String, RvColumn> rvColumnMap, Map<String, RvIndex> rvIndexMap, Map<String, RvForeignKey> rvForeignKeyMap) {
        RvForeignKey rvForeignKey = rvForeignKeyMap.get(foreignKey.getName());
        boolean isNew = rvForeignKey == null;
        if (rvForeignKey == null) {
            rvForeignKey = new RvForeignKey();
            rvForeignKeyMap.put(foreignKey.getName(), rvForeignKey);
        }
        setCodeAndTitleAndBuiltIn(rvForeignKey, foreignKey);
        // 是否级联删除
        rvForeignKey.setCascadeDelete(importedKeyCascade.equals(foreignKey.getDeleteRule()));
        if (foreignKey.getBackingIndex() != null) {
            rvForeignKey.setBackingIndex(rvIndexMap.get(foreignKey.getBackingIndex().getName()));
        }
        final RvForeignKey targetRvForeignKey = rvForeignKey;
        rvForeignKey.setForeignKeyTargetColumns(Streams.mapWithIndex(foreignKey.getPrimaryKeyColumns().stream(), (column, idx) -> {
            List<RvForeignKeyTargetColumn> matchedRvForeignKeyTargetColumns = isNew ? null : targetRvForeignKey.getForeignKeyTargetColumns().stream()
                    .filter(foreignKeyTargetColumn -> column.getName().equals(foreignKeyTargetColumn.getColumn().getCode()))
                    .toList();
            RvForeignKeyTargetColumn rvForeignKeyTargetColumn;
            if (matchedRvForeignKeyTargetColumns != null && !matchedRvForeignKeyTargetColumns.isEmpty()) {
                rvForeignKeyTargetColumn = matchedRvForeignKeyTargetColumns.get(0);
            } else {
                rvForeignKeyTargetColumn = new RvForeignKeyTargetColumn();
                rvForeignKeyTargetColumn.setBuiltIn(true);
                rvForeignKeyTargetColumn.setForeignKey(targetRvForeignKey);
                rvForeignKeyTargetColumn.setColumn(rvColumnMap.get(column.getName()));
                rvForeignKeyTargetColumn.setOrderNum((int) idx);
            }
            return rvForeignKeyTargetColumn;
        }).collect(Collectors.toList()));
        rvForeignKey.setTargetPrototype(rvPrototypeMap.get(foreignKey.getPrimaryKeyTable().getName()));
        rvForeignKey.setForeignKeyForeignColumns(Streams.mapWithIndex(foreignKey.getForeignKeyColumns().stream(), (column, idx) -> {
            List<RvForeignKeyForeignColumn> matchedRvForeignKeyForeignColumns = isNew ? null : targetRvForeignKey.getForeignKeyForeignColumns().stream()
                    .filter(foreignKeyForeignColumn -> column.getName().equals(foreignKeyForeignColumn.getColumn().getCode()))
                    .toList();
            RvForeignKeyForeignColumn rvForeignKeyForeignColumn;
            if (matchedRvForeignKeyForeignColumns != null && !matchedRvForeignKeyForeignColumns.isEmpty()) {
                rvForeignKeyForeignColumn = matchedRvForeignKeyForeignColumns.get(0);
            } else {
                rvForeignKeyForeignColumn = new RvForeignKeyForeignColumn();
                rvForeignKeyForeignColumn.setBuiltIn(true);
                rvForeignKeyForeignColumn.setForeignKey(targetRvForeignKey);
                rvForeignKeyForeignColumn.setColumn(rvColumnMap.get(column.getName()));
                rvForeignKeyForeignColumn.setOrderNum((int) idx);
            }
            return rvForeignKeyForeignColumn;
        }).collect(Collectors.toList()));
        return rvForeignKey;
    }

    private RvUnique buildRvUnique(UniqueConstraint uniqueConstraint, Map<String, RvColumn> rvColumnMap, Map<String, RvIndex> rvIndexMap, Map<String, RvUnique> rvUniqueMap) {
        RvUnique rvUnique = rvUniqueMap.get(uniqueConstraint.getName());
        boolean isNew = rvUnique == null;
        if (rvUnique == null) {
            rvUnique = new RvUnique();
            rvUniqueMap.put(uniqueConstraint.getName(), rvUnique);
        }
        setCodeAndTitleAndBuiltIn(rvUnique, uniqueConstraint);
        if (uniqueConstraint.getBackingIndex() != null) {
            rvUnique.setBackingIndex(rvIndexMap.get(uniqueConstraint.getBackingIndex().getName()));
        }
        final RvUnique targetRvUnique = rvUnique;
        rvUnique.setUniqueColumns(Streams.mapWithIndex(uniqueConstraint.getColumns().stream(), (column, idx) -> {
            List<RvUniqueColumn> matchedRvUniqueColumns = isNew ? null : targetRvUnique.getUniqueColumns().stream()
                    .filter(uniqueColumn -> column.getName().equals(uniqueColumn.getColumn().getCode()))
                    .toList();
            RvUniqueColumn rvUniqueColumn;
            if (matchedRvUniqueColumns != null && !matchedRvUniqueColumns.isEmpty()) {
                rvUniqueColumn = matchedRvUniqueColumns.get(0);
            } else {
                rvUniqueColumn = new RvUniqueColumn();
                rvUniqueColumn.setBuiltIn(true);
                rvUniqueColumn.setUnique(targetRvUnique);
                rvUniqueColumn.setColumn(rvColumnMap.get(column.getName()));
                rvUniqueColumn.setOrderNum((int) idx);
            }
            return rvUniqueColumn;
        }).collect(Collectors.toList()));
        return rvUnique;
    }

    private RvNotNull buildRvNotNull(NotNullConstraint notNullConstraint, Map<String, RvColumn> rvColumnMap, Map<String, RvNotNull> rvNotNullMap) {
        RvNotNull rvNotNull = rvNotNullMap.get(notNullConstraint.getConstraintName());
        if (rvNotNull == null) {
            rvNotNull = new RvNotNull();
            rvNotNullMap.put(notNullConstraint.getConstraintName(), rvNotNull);
        }
        // NotNullConstraint比较特殊，不是DatabaseObject的子类
        rvNotNull.setBuiltIn(true);
        if (rvNotNull.getCode() == null) {
            rvNotNull.setCode(notNullConstraint.getConstraintName());
        }
        String columnName = notNullConstraint.getColumnName();
        if (rvNotNull.getTitle() == null) {
            rvNotNull.setTitle(columnName);
        }
        rvNotNull.setColumn(rvColumnMap.get(columnName));
        return rvNotNull;
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
                    List<org.hibernate.mapping.Column> columnList = property.getValue().getColumns();
                    String columnName = columnList.get(0).getText();
                    this.columnFieldMap.put(tableName + "." + columnName, field);
                }
            });
        }
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
