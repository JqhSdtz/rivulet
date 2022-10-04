package org.laputa.rivulet.module.datamodel.service;

import liquibase.Liquibase;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.exception.LiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import lombok.extern.slf4j.Slf4j;
import org.laputa.rivulet.common.model.Result;
import org.laputa.rivulet.common.util.RedissonLockUtil;
import org.laputa.rivulet.module.datamodel.entity.RvColumn;
import org.laputa.rivulet.module.datamodel.entity.RvIndex;
import org.laputa.rivulet.module.datamodel.entity.RvIndexColumn;
import org.laputa.rivulet.module.datamodel.entity.RvPrototype;
import org.laputa.rivulet.module.datamodel.repository.RvPrototypeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
            Map<String, RvPrototype> prototypeMap = new HashMap<>(prototypeList.size());
            prototypeList.forEach(prototype -> prototypeMap.put(prototype.getCode(), prototype));
            List<RvPrototype> targetRvPrototypeList = new ArrayList<>(tableList.size());
            tableList.forEach(table -> {
                RvPrototype oriPrototype = prototypeMap.get(table.getName());
                if (oriPrototype == null) {
                    targetRvPrototypeList.add(buildRvPrototype(table));
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

    private RvPrototype buildRvPrototype(Table table) {
        RvPrototype rvPrototype = new RvPrototype();
        rvPrototype.setName(table.getName());
        rvPrototype.setCode(table.getName());
        rvPrototype.setRemark(table.getRemarks());
        rvPrototype.setColumns(table.getColumns().stream().map(column -> {
            RvColumn rvColumn = buildRvColumn(column);
            rvColumn.setPrototype(rvPrototype);
            return rvColumn;
        }).collect(Collectors.toList()));
        rvPrototype.setIndexes(table.getIndexes().stream().map(index -> {
            RvIndex rvIndex = buildRvIndex(index, rvPrototype.getColumns());
            rvIndex.setPrototype(rvPrototype);
            return rvIndex;
        }).collect(Collectors.toList()));
        rvPrototype.setDbSyncFlag(true);
        return rvPrototype;
    }

    private RvColumn buildRvColumn(Column column) {
        RvColumn rvColumn = new RvColumn();
        rvColumn.setName(column.getName());
        rvColumn.setCode(column.getName());
        rvColumn.setDataType(column.getType().toString());
        rvColumn.setOrder(column.getOrder());
        if (column.getDefaultValue() != null) {
            rvColumn.setDefaultValue(column.getDefaultValue().toString());
        }
        rvColumn.setRemark(column.getRemarks());
        return rvColumn;
    }

    private RvIndex buildRvIndex(Index index, List<RvColumn> rvColumns) {
        RvIndex rvIndex = new RvIndex();
        Map<String, RvColumn> rvColumnMap = new HashMap<>(rvColumns.size());
        rvColumns.forEach(rvColumn -> rvColumnMap.put(rvColumn.getCode(), rvColumn));
        rvIndex.setName(index.getName());
        rvIndex.setCode(index.getName());
        rvIndex.setIndexColumns(index.getColumns().stream().map(column -> {
            RvIndexColumn rvIndexColumn = new RvIndexColumn();
            rvIndexColumn.setIndex(rvIndex);
            rvIndexColumn.setColumn(rvColumnMap.get(column.getName()));
            return rvIndexColumn;
        }).collect(Collectors.toList()));
        return rvIndex;
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
