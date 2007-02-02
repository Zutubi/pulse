package com.zutubi.prototype.form;

import com.zutubi.prototype.form.model.Table;
import com.zutubi.prototype.form.model.Column;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class TableDescriptor implements Descriptor
{
    private String name;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    private List<ColumnDescriptor> columnDescriptors = new LinkedList<ColumnDescriptor>();

    public void addParameter(String key, Object value)
    {
        parameters.put(key, value);
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public Object instantiate(Object obj)
    {
        Table table = new Table();

        for (ColumnDescriptor columnDescriptor : columnDescriptors)
        {
            Column column = new Column();
            column.setName(columnDescriptor.getName());
            table.addColumn(column);
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
}
