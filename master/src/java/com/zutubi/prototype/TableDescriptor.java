package com.zutubi.prototype;

import com.zutubi.prototype.model.Table;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class TableDescriptor implements Descriptor
{
    private String name;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    private List<ColumnDescriptor> columnDescriptors = new LinkedList<ColumnDescriptor>();
    private List<RowDescriptor> rowDescriptors = new LinkedList<RowDescriptor>();

    public void addParameter(String key, Object value)
    {
        parameters.put(key, value);
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public Table instantiate(Object obj)
    {
        Table table = new Table();

        for (RowDescriptor rowDescriptor : rowDescriptors)
        {
            table.addRows(rowDescriptor.instantiate(obj));
        }

        return table;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void addDescriptor(ColumnDescriptor columnDescriptor)
    {
        columnDescriptors.add(columnDescriptor);
    }

    public List<ColumnDescriptor> getColumnDescriptors()
    {
        return columnDescriptors;
    }

    public void addDescriptor(RowDescriptor rowDescriptor)
    {
        rowDescriptors.add(rowDescriptor);
    }

    public List<RowDescriptor> getRowDescriptors()
    {
        return rowDescriptors;
    }
}
