package com.zutubi.pulse.transfer;

import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashMap;

/**
 *
 *
 */
public class TransferTable implements Table
{
    private String name;
    private List<Column> columns;

    private Map<String, Integer> columnTypes;
    private Map<String, Column> columnsByName;

    public TransferTable()
    {

    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Column> getColumns()
    {
        if (columns == null)
        {
            columns = new LinkedList<Column>();
        }
        return columns;
    }

    private Map<String, Integer> getColumnTypes()
    {
        if (columnTypes == null)
        {
            columnTypes = new HashMap<String, Integer>();
        }
        return columnTypes;
    }

    private Map<String, Column> getColumnsByName()
    {
        if (columnsByName == null)
        {
            columnsByName = new HashMap<String, Column>();
        }
        return columnsByName;
    }

    public void add(Column column)
    {
        getColumns().add(column);
        getColumnTypes().put(column.getName(), column.getSqlTypeCode());
        getColumnsByName().put(column.getName(), column);
    }

    public Integer getColumnType(String name)
    {
        return getColumnTypes().get(name);
    }

    public Column getColumn(String name)
    {
        return getColumnsByName().get(name);
    }

    public void remove(Column column)
    {
        this.columns.remove(column);
        this.columnsByName.remove(column.getName());
        this.columnTypes.remove(column.getName());
    }

    public TransferTable copy()
    {
        TransferTable copy = new TransferTable();
        copy.name = this.name;
        copy.columns = new LinkedList<Column>(this.columns);
        copy.columnsByName = new HashMap<String, Column>(this.columnsByName);
        copy.columnTypes = new HashMap<String, Integer>(this.columnTypes);

        return copy;
    }
}
