package org.laputa.rivulet.ddl;

import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.DatabaseDataType;
import org.laputa.rivulet.module.data_model.entity.RvColumn;
import org.laputa.rivulet.module.data_model.entity.constraint.RvUnique;

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

    public ConstraintsConfig toConstraintsConfig(RvUnique rvUnique) {
        ConstraintsConfig constraintsConfig = new ConstraintsConfig();
        constraintsConfig.setUnique(true);
        constraintsConfig.setReferencedColumnNames(rvUnique.getUniqueColumns()
                .stream().map(column -> column.getColumn().getName()).collect(Collectors.joining(",")));
        return constraintsConfig;
    }
}
