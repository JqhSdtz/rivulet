package org.laputa.rivulet.liquibase.snapshot;

import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;
import org.laputa.rivulet.liquibase.database.RivuletDatabase;
import org.laputa.rivulet.module.dbms_model.entity.RvColumn;
import org.laputa.rivulet.module.dbms_model.entity.RvTable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Columns are snapshotted along with Tables in {@link TableSnapshotGenerator} but this class needs to be here to keep the default ColumnSnapshotGenerator from running.
 * Ideally the column logic would be moved out of the TableSnapshotGenerator to better work in situations where the object types to snapshot are being controlled, but that is not the case yet.
 */
public class ColumnSnapshotGenerator extends RivuletSnapshotGenerator {

    private final static Pattern pattern = Pattern.compile("([^\\(]*)\\s*\\(?\\s*(\\d*)?\\s*,?\\s*(\\d*)?\\s*([^\\(]*?)\\)?");

    public ColumnSnapshotGenerator() {
        super(Column.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Column column = (Column) example;
        if (column.getType() == null) { //not the actual full version found with the table
            if (column.getRelation() == null) {
                throw new InvalidExampleException("No relation set on " + column);
            }
            Relation relation = snapshot.get(column.getRelation());
            if (relation != null) {
                for (Column columnSnapshot : relation.getColumns()) {
                    if (columnSnapshot.getName().equalsIgnoreCase(column.getName())) {
                        return columnSnapshot;
                    }
                }
            }
            snapshotColumn((Column) example, snapshot);
        }
        return example; //did not find it
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (foundObject instanceof Table) {
            RvTable rvTable = findRvTable(foundObject, snapshot);
            if (rvTable == null) {
                return;
            }
            for (RvColumn rvColumn : rvTable.getColumns()) {
                Column column = new Column();
                column.setName(rvColumn.getCode());
                column.setRelation((Table) foundObject);
                snapshotColumn(column, snapshot);
                ((Table) foundObject).getColumns().add(column);
            }
        }
    }

    protected void snapshotColumn(Column column, DatabaseSnapshot snapshot) throws DatabaseException {
        RivuletDatabase database = (RivuletDatabase) snapshot.getDatabase();
        RvTable rvTable = findRvTable(column.getRelation(), snapshot);
        if (rvTable == null) {
            return;
        }
        for (RvColumn rvColumn : rvTable.getColumns()) {
            if (rvColumn.getCode().equalsIgnoreCase(column.getName())) {
                String rvColumnDataType = rvColumn.getDataType();
                String rvColumnDefaultValue = rvColumn.getDefaultValue();
                Matcher defaultValueMatcher = Pattern.compile("(?i) DEFAULT\\s+(.*)").matcher(rvColumnDataType);
                if (defaultValueMatcher.find()) {
                    rvColumnDefaultValue = defaultValueMatcher.group(1);
                    rvColumnDataType = rvColumnDataType.replace(defaultValueMatcher.group(0), "");
                }
                Integer sqlTypeCode = database.resolveSqlTypeCode(rvColumnDataType);
                DataType dataType = toDataType(rvColumnDataType, sqlTypeCode);
                if (dataType == null) {
                    throw new DatabaseException("Unable to find column data type for column " + rvColumn.getCode());
                }
                column.setType(dataType);
                column.setRemarks(rvColumn.getRemark());
                column.setDefaultValue(rvColumnDefaultValue);
                column.setNullable(rvColumn.getNullable());
                column.setCertainDataType(false);
                return;
            }
        }
    }

    protected DataType toDataType(String rvColumnDataType, Integer sqlTypeCode) {
        Matcher matcher = pattern.matcher(rvColumnDataType);
        if (!matcher.matches()) {
            return null;
        }
        DataType dataType = new DataType(matcher.group(1));
        if (matcher.group(3).isEmpty()) {
            if (!matcher.group(2).isEmpty()) {
                dataType.setColumnSize(Integer.parseInt(matcher.group(2)));
            }
        } else {
            dataType.setColumnSize(Integer.parseInt(matcher.group(2)));
            dataType.setDecimalDigits(Integer.parseInt(matcher.group(3)));
        }
        String extra = StringUtil.trimToNull(matcher.group(4));
        // !!!添加默认的size unit，防止类型比对的时候出现错误
        dataType.setColumnSizeUnit(DataType.ColumnSizeUnit.BYTE);
        if (extra != null) {
            if (extra.equalsIgnoreCase("char")) {
                dataType.setColumnSizeUnit(DataType.ColumnSizeUnit.CHAR);
            }
        }
        dataType.setDataTypeId(sqlTypeCode);
        return dataType;
    }

}
