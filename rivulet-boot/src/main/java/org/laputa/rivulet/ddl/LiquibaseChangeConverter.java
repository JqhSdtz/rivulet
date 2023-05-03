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
    private Database database;

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
        columnConfig.setName(rvColumn.getName())
                .setType(rvColumn.getDataType())
                .setDefaultValue(rvColumn.getDefaultValue());
        return columnConfig;
    }

    public AddColumnConfig addColumnConfig(RvColumn rvColumn) {
        AddColumnConfig config = new AddColumnConfig();
        config.setName(rvColumn.getName()).setType(rvColumn.getDataType())
                .setDefaultValue(rvColumn.getDefaultValue());
        return config;
    }

    public AddForeignKeyConstraintChange addForeignKeyConstraint(RvForeignKey rvForeignKey) {
        AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
        change.setConstraintName(rvForeignKey.getName());
        change.setDeleteCascade(rvForeignKey.getCascadeDelete());
        change.setReferencedTableName(rvForeignKey.getTargetPrototype().getName());
        change.setReferencedColumnNames(rvForeignKey.getForeignKeyForeignColumns()
                .stream().map(column -> column.getColumn().getName()).collect(Collectors.joining(",")));
        change.setBaseTableName(rvForeignKey.getPrototype().getName());
        change.setBaseColumnNames(rvForeignKey.getForeignKeyTargetColumns()
                .stream().map(column -> column.getColumn().getName()).collect(Collectors.joining(",")));
        return change;
    }

    public DropForeignKeyConstraintChange dropForeignKeyConstraint(RvForeignKey rvForeignKey) {
        DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
        change.setBaseTableName(rvForeignKey.getPrototype().getName());
        change.setConstraintName(rvForeignKey.getName());
        return change;
    }

    public AddPrimaryKeyChange addPrimaryKey(RvPrimaryKey rvPrimaryKey) {
        AddPrimaryKeyChange change = new AddPrimaryKeyChange();
        change.setTableName(rvPrimaryKey.getPrototype().getName());
        change.setConstraintName(rvPrimaryKey.getName());
        change.setColumnNames(rvPrimaryKey.getPrimaryKeyColumns()
                .stream().map(column -> column.getColumn().getName()).collect(Collectors.joining(",")));
        return change;
    }

    public DropPrimaryKeyChange dropPrimaryKey(RvPrimaryKey rvPrimaryKey) {
        DropPrimaryKeyChange change = new DropPrimaryKeyChange();
        change.setTableName(rvPrimaryKey.getPrototype().getName());
        change.setConstraintName(rvPrimaryKey.getName());
        return change;
    }

    public AddNotNullConstraintChange addNotNullConstraint(RvNotNull rvNotNull) {
        AddNotNullConstraintChange change = new AddNotNullConstraintChange();
        change.setTableName(rvNotNull.getPrototype().getName());
        change.setConstraintName(rvNotNull.getName());
        change.setColumnName(rvNotNull.getColumn().getName());
        return change;
    }

    public DropNotNullConstraintChange dropNotNullConstraint(RvNotNull rvNotNull) {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName(rvNotNull.getPrototype().getName());
        change.setConstraintName(rvNotNull.getName());
        return change;
    }

    public AddUniqueConstraintChange addUniqueConstraint(RvUnique rvUnique) {
        AddUniqueConstraintChange change = new AddUniqueConstraintChange();
        change.setTableName(rvUnique.getPrototype().getName());
        change.setConstraintName(rvUnique.getName());
        change.setColumnNames(rvUnique.getUniqueColumns()
                .stream().map(column -> column.getColumn().getName()).collect(Collectors.joining(",")));
        return change;
    }

    public DropUniqueConstraintChange dropUniqueConstraint(RvUnique rvUnique) {
        DropUniqueConstraintChange change = new DropUniqueConstraintChange();
        change.setTableName(rvUnique.getPrototype().getName());
        change.setConstraintName(rvUnique.getName());
        return change;
    }

    public CreateIndexChange createIndex(RvIndex rvIndex) {
        CreateIndexChange change = new CreateIndexChange();
        change.setTableName(rvIndex.getPrototype().getName());
        change.setIndexName(rvIndex.getName());
        change.setColumns(rvIndex.getIndexColumns().stream().map(rvIndexColumn -> {
            RvColumn rvColumn = rvIndexColumn.getColumn();
            return addColumnConfig(rvColumn);
        }).collect(Collectors.toList()));
        return change;
    }

    public DropIndexChange dropIndex(RvIndex rvIndex) {
        DropIndexChange change = new DropIndexChange();
        change.setTableName(rvIndex.getPrototype().getName());
        change.setIndexName(rvIndex.getName());
        return change;
    }

    public AddColumnChange addColumn(List<RvColumn> rvColumnList) {
        AddColumnChange change = new AddColumnChange();
        if (rvColumnList == null || rvColumnList.size() == 0) return change;
        change.setTableName(rvColumnList.get(0).getPrototype().getName());
        change.setColumns(rvColumnList.stream().map(rvColumn -> addColumnConfig(rvColumn))
                .collect(Collectors.toList()));
        return change;
    }

    public DropColumnChange dropColumn(List<RvColumn> rvColumnList) {
        DropColumnChange change = new DropColumnChange();
        if (rvColumnList == null || rvColumnList.size() == 0) return change;
        change.setTableName(rvColumnList.get(0).getPrototype().getName());
        change.setColumns(rvColumnList.stream().map(rvColumn -> addColumnConfig(rvColumn))
                .collect(Collectors.toList()));
        return change;
    }

    public CreateTableChange createTable(RvPrototype rvPrototype) {
        CreateTableChange change = new CreateTableChange();
        change.setTableName(rvPrototype.getName());
        change.setRemarks(rvPrototype.getRemark());
        change.setColumns(rvPrototype.getColumns().stream().map(rvColumn -> addColumnConfig(rvColumn))
                .collect(Collectors.toList()));
        return change;
    }

    public DropTableChange dropTable(RvPrototype rvPrototype) {
        DropTableChange change = new DropTableChange();
        change.setTableName(rvPrototype.getName());
        return change;
    }

}
