package liquibase.ext.hibernate.snapshot;

import liquibase.Scope;
import liquibase.exception.DatabaseException;
import liquibase.ext.hibernate.DatabaseObjectAttrName;
import liquibase.ext.hibernate.GlobalSetting;
import liquibase.ext.hibernate.annotation.Title;
import liquibase.ext.hibernate.util.IndexStoreUtil;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtil;
import org.hibernate.HibernateException;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UniqueConstraintSnapshotGenerator extends HibernateSnapshotGenerator {

    public UniqueConstraintSnapshotGenerator() {
        super(UniqueConstraint.class, new Class[]{Table.class});
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        return example;
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!snapshot.getSnapshotControl().shouldInclude(UniqueConstraint.class)) {
            return;
        }

        if (foundObject instanceof Table) {
            Table table = (Table) foundObject;
            org.hibernate.mapping.Table hibernateTable = findHibernateTable(table, snapshot);
            if (hibernateTable == null) {
                return;
            }
            for (var hibernateUnique : hibernateTable.getUniqueKeys().values()) {
                UniqueConstraint uniqueConstraint = new UniqueConstraint();
                uniqueConstraint.setName(hibernateUnique.getName());
                uniqueConstraint.setRelation(table);
                uniqueConstraint.setClustered(false); // No way to set true via Hibernate

                int i = 0;
                // !!!给unique设置title
                StringBuilder stringBuilder = new StringBuilder();
                boolean first = true;
                for (var hibernateColumn : hibernateUnique.getColumns()) {
                    uniqueConstraint.addColumn(i++, new Column(hibernateColumn.getName()).setRelation(table));
                    Field columnField = getColumnField(snapshot, table.getName(), hibernateColumn.getName());
                    if (columnField != null && columnField.isAnnotationPresent(Title.class)) {
                        Title title = columnField.getAnnotation(Title.class);
                        stringBuilder.append(first ? "" : "、").append(title.value());
                        first = false;
                    }
                }
                stringBuilder.append("的唯一性约束");
                uniqueConstraint.setAttribute(DatabaseObjectAttrName.Title, stringBuilder.toString());

                Index index = getBackingIndex(uniqueConstraint, hibernateTable, snapshot);
                uniqueConstraint.setBackingIndex(index);

                // !!!全局控制found信息输出
                if (GlobalSetting.isShowFoundInfo()) {
                    Scope.getCurrentScope().getLog(getClass()).info("Found unique constraint " + uniqueConstraint);
                }
                table.getUniqueConstraints().add(uniqueConstraint);
            }
            for (var column : hibernateTable.getColumns()) {
                if (column.isUnique()) {
                    UniqueConstraint uniqueConstraint = new UniqueConstraint();
                    uniqueConstraint.setRelation(table);
                    uniqueConstraint.setClustered(false); // No way to set true via Hibernate
                    String name = "UC_" + table.getName().toUpperCase() + column.getName().toUpperCase() + "_COL";
                    if (name.length() > 64) {
                        name = name.substring(0, 63);
                    }
                    uniqueConstraint.addColumn(0, new Column(column.getName()).setRelation(table));
                    uniqueConstraint.setName(name);
                    // !!!全局控制found信息输出
                    if (GlobalSetting.isShowFoundInfo()) {
                        Scope.getCurrentScope().getLog(getClass()).info("Found unique constraint " + uniqueConstraint);
                    }
                    table.getUniqueConstraints().add(uniqueConstraint);

                    //!!!给unique设置title
                    Field columnField = getColumnField(snapshot, table.getName(), column.getName());
                    if (columnField != null && columnField.isAnnotationPresent(Title.class)) {
                        Title title = columnField.getAnnotation(Title.class);
                        uniqueConstraint.setAttribute(DatabaseObjectAttrName.Title, title.value() + "的唯一性约束");
                    }
                    Index index = getBackingIndex(uniqueConstraint, hibernateTable, snapshot);
                    uniqueConstraint.setBackingIndex(index);

                }
            }

            for (UniqueConstraint uc : table.getUniqueConstraints()) {
                if (uc.getName() == null || uc.getName().isEmpty()) {
                    String name = table.getName() + uc.getColumnNames();
                    name = "UCIDX" + hashedName(name);
                    uc.setName(name);
                }
            }
        }
    }

    private String hashedName(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.reset();
            md.update(s.getBytes());
            byte[] digest = md.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            // By converting to base 35 (full alphanumeric), we guarantee
            // that the length of the name will always be smaller than the 30
            // character identifier restriction enforced by a few dialects.
            return bigInt.toString(35);
        } catch (NoSuchAlgorithmException e) {
            throw new HibernateException("Unable to generate a hashed name!", e);
        }
    }

    protected Index getBackingIndex(UniqueConstraint uniqueConstraint, org.hibernate.mapping.Table hibernateTable, DatabaseSnapshot snapshot) {
        // !!!不知道为啥，之前没有将这个索引加到table里，修改了一下，都加到table的索引里
        Index index;
        // 如果此前已经有对应的index，则直接获取原本的index。该index可能是由foreignKey或primaryKey创建而来
        Index oriIndex = IndexStoreUtil.getIndex(uniqueConstraint.getColumns());
        if (oriIndex != null) {
            oriIndex.setUnique(true);
            index = oriIndex;
        } else {
            index = new Index();
            IndexStoreUtil.addIndex(uniqueConstraint.getColumns(), index);
        }
        Table table = (Table) uniqueConstraint.getRelation();
        index.setRelation(table);
        index.setColumns(uniqueConstraint.getColumns());
        index.setUnique(true);
        index.setName(String.format("%s_%s_IX",hibernateTable.getName(), StringUtil.randomIdentifier(4)));
        index.setAttribute(DatabaseObjectAttrName.Title, uniqueConstraint.getAttribute(DatabaseObjectAttrName.Title, String.class) + "的索引");
        table.getIndexes().add(index);


        return index;
    }

}
