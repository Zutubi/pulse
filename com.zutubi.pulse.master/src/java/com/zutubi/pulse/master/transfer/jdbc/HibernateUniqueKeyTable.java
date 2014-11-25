package com.zutubi.pulse.master.transfer.jdbc;

import org.hibernate.mapping.Column;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;

import java.sql.Types;

/**
 *
 *
 */
public class HibernateUniqueKeyTable
{
    public static boolean isTable(String tableName)
    {
        return tableName.equals("hibernate_unique_key");
    }

    public static Table getMapping()
    {
        Table table = new Table("hibernate_unique_key");
        Column column = new Column("next_hi");
        SimpleValue value = new SimpleValue(null, table);
        value.setTypeName(int.class.getName());
        column.setValue(value);
        column.setSqlTypeCode(Types.INTEGER);
        table.addColumn(column);
        return table;
    }
}
