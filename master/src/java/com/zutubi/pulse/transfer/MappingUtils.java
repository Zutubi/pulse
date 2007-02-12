package com.zutubi.pulse.transfer;

import org.hibernate.mapping.Table;
import org.hibernate.mapping.Column;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

/**
 *
 *
 */
public class MappingUtils
{
    public static String sqlSelectAll(Table table)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("select");
        String sep = " ";

        for (Column column : getColumns(table))
        {
            builder.append(sep).append(column.getName());
            sep = ", ";
        }
        builder.append(" from ").append(table.getName());
        return builder.toString();
    }

    public static String sqlInsert(Table table)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ").append(table.getName());
        String sep = " (";

        for (Column column : getColumns(table))
        {
            builder.append(sep).append(column.getName());
            sep = ", ";
        }
        builder.append(") values (");

        sep = "";
        for (Column column : getColumns(table))
        {
            builder.append(sep).append("?");
            sep = ", ";
        }
        builder.append(")");
        return builder.toString();
    }


    public static List<Column> getColumns(Table table)
    {
        List<Column> columns = new LinkedList<Column>();
        Iterator columnIterator = table.getColumnIterator();
        while (columnIterator.hasNext())
        {
            columns.add((Column) columnIterator.next());
        }
        return columns;
    }

}
