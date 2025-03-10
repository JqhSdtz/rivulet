package org.laputa.rivulet.ddl;

import liquibase.change.AddColumnConfig;
import liquibase.change.ColumnConfig;
import liquibase.change.core.*;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.DatabaseDataType;
import org.laputa.rivulet.module.data_model.entity.RvColumn;
import org.laputa.rivulet.module.data_model.entity.RvIndex;
import org.laputa.rivulet.module.data_model.entity.RvPrototype;
import org.laputa.rivulet.module.data_model.entity.constraint.RvForeignKey;
import org.laputa.rivulet.module.data_model.entity.constraint.RvNotNull;
import org.laputa.rivulet.module.data_model.entity.constraint.RvPrimaryKey;
import org.laputa.rivulet.module.data_model.entity.constraint.RvUnique;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JQH
 * @since 下午 6:17 23/04/15
 */

public class LiquibaseChangeConverter {
    private final Database database;

    public LiquibaseChangeConverter(Database database) {
        this.database = database;
    }

    public String convertDataTypeToSqlType(String dataType, boolean isAutoIncrement) {
        String definition = dataType + (isAutoIncrement ? "{autoIncrement:true}" : "");
        DatabaseDataType columnType = DataTypeFactory.getInstance().fromDescription(definition, this.database).toDatabaseDataType(this.database);
        return columnType.toSql();
    }

    public ColumnConfig toColumnConfig(RvColumn rvColumn) {
        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName(rvColumn.getCode())
                .setType(rvColumn.getDataType())
                .setDefaultValue(rvColumn.getDefaultValue());
        return columnConfig;
    }

    public AddColumnConfig addColumnConfig(RvColumn rvColumn) {
        AddColumnConfig config = new AddColumnConfig();
        config.setName(rvColumn.getCode()).setType(rvColumn.getDataType())
                .setDefaultValue(rvColumn.getDefaultValue());
        return config;
    }

    public AddForeignKeyConstraintChange addForeignKeyConstraint(RvForeignKey rvForeignKey) {
        AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
        change.setConstraintName(rvForeignKey.getCode());
        change.setDeleteCascade(rvForeignKey.getCascadeDelete());
        change.setReferencedTableName(rvForeignKey.getTargetPrototype().getCode());
        change.setReferencedColumnNames(rvForeignKey.getForeignKeyForeignColumns()
                .stream().map(column -> column.getColumn().getCode()).collect(Collectors.joining(",")));
        change.setBaseTableName(rvForeignKey.getPrototype().getCode());
        change.setBaseColumnNames(rvForeignKey.getForeignKeyTargetColumns()
                .stream().map(column -> column.getColumn().getCode()).collect(Collectors.joining(",")));
        return change;
    }

    public DropForeignKeyConstraintChange dropForeignKeyConstraint(RvForeignKey rvForeignKey) {
        DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
        change.setBaseTableName(rvForeignKey.getPrototype().getCode());
        change.setConstraintName(rvForeignKey.getCode());
        return change;
    }

    public AddPrimaryKeyChange addPrimaryKey(RvPrimaryKey rvPrimaryKey) {
        AddPrimaryKeyChange change = new AddPrimaryKeyChange();
        change.setTableName(rvPrimaryKey.getPrototype().getCode());
        change.setConstraintName(rvPrimaryKey.getCode());
        change.setColumnNames(rvPrimaryKey.getPrimaryKeyColumns()
                .stream().map(column -> column.getColumn().getCode()).collect(Collectors.joining(",")));
        return change;
    }

    public DropPrimaryKeyChange dropPrimaryKey(RvPrimaryKey rvPrimaryKey) {
        DropPrimaryKeyChange change = new DropPrimaryKeyChange();
        change.setTableName(rvPrimaryKey.getPrototype().getCode());
        change.setConstraintName(rvPrimaryKey.getCode());
        return change;
    }

    public AddNotNullConstraintChange addNotNullConstraint(RvNotNull rvNotNull) {
        AddNotNullConstraintChange change = new AddNotNullConstraintChange();
        change.setTableName(rvNotNull.getPrototype().getCode());
        change.setConstraintName(rvNotNull.getCode());
        change.setColumnName(rvNotNull.getColumn().getCode());
        return change;
    }

    public DropNotNullConstraintChange dropNotNullConstraint(RvNotNull rvNotNull) {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName(rvNotNull.getPrototype().getCode());
        change.setConstraintName(rvNotNull.getCode());
        return change;
    }

    public AddUniqueConstraintChange addUniqueConstraint(RvUnique rvUnique) {
        AddUniqueConstraintChange change = new AddUniqueConstraintChange();
        change.setTableName(rvUnique.getPrototype().getCode());
        change.setConstraintName(rvUnique.getCode());
        change.setColumnNames(rvUnique.getUniqueColumns()
                .stream().map(column -> column.getColumn().getCode()).collect(Collectors.joining(",")));
        return change;
    }

    public DropUniqueConstraintChange dropUniqueConstraint(RvUnique rvUnique) {
        DropUniqueConstraintChange change = new DropUniqueConstraintChange();
        change.setTableName(rvUnique.getPrototype().getCode());
        change.setConstraintName(rvUnique.getCode());
        return change;
    }

    public CreateIndexChange createIndex(RvIndex rvIndex) {
        CreateIndexChange change = new CreateIndexChange();
        change.setTableName(rvIndex.getPrototype().getCode());
        change.setIndexName(rvIndex.getCode());
        change.setColumns(rvIndex.getIndexColumns().stream().map(rvIndexColumn -> {
            RvColumn rvColumn = rvIndexColumn.getColumn();
            return addColumnConfig(rvColumn);
        }).collect(Collectors.toList()));
        return change;
    }

    public DropIndexChange dropIndex(RvIndex rvIndex) {
        DropIndexChange change = new DropIndexChange();
        change.setTableName(rvIndex.getPrototype().getCode());
        change.setIndexName(rvIndex.getCode());
        return change;
    }

    public AddColumnChange addColumn(List<RvColumn> rvColumnList) {
        AddColumnChange change = new AddColumnChange();
        if (rvColumnList == null || rvColumnList.size() == 0) return change;
        change.setTableName(rvColumnList.get(0).getPrototype().getCode());
        change.setColumns(rvColumnList.stream().map(rvColumn -> addColumnConfig(rvColumn))
                .collect(Collectors.toList()));
        return change;
    }

    public DropColumnChange dropColumn(List<RvColumn> rvColumnList) {
        DropColumnChange change = new DropColumnChange();
        if (rvColumnList == null || rvColumnList.size() == 0) return change;
        change.setTableName(rvColumnList.get(0).getPrototype().getCode());
        change.setColumns(rvColumnList.stream().map(rvColumn -> addColumnConfig(rvColumn))
                .collect(Collectors.toList()));
        return change;
    }

    public CreateTableChange createTable(RvPrototype rvPrototype) {
        CreateTableChange change = new CreateTableChange();
        change.setTableName(rvPrototype.getCode());
        change.setRemarks(rvPrototype.getRemark());
        change.setColumns(rvPrototype.getColumns().stream().map(rvColumn -> addColumnConfig(rvColumn))
                .collect(Collectors.toList()));
        return change;
    }

    public DropTableChange dropTable(RvPrototype rvPrototype) {
        DropTableChange change = new DropTableChange();
        change.setTableName(rvPrototype.getCode());
        return change;
    }

}
