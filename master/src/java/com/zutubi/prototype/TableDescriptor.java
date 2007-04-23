package com.zutubi.prototype;

import com.zutubi.prototype.model.Table;
import com.zutubi.prototype.type.record.Record;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class TableDescriptor extends AbstractDescriptor
{
    private String name;
    private List<RowDescriptor> rowDescriptors = new LinkedList<RowDescriptor>();

    public Table instantiate(String path, Record obj)
    {
        Table table = new Table();

        for (RowDescriptor rowDescriptor : rowDescriptors)
        {
            table.addRows(rowDescriptor.instantiate(path, obj));
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

    public void addDescriptor(RowDescriptor rowDescriptor)
    {
        rowDescriptors.add(rowDescriptor);
    }

    public List<RowDescriptor> getRowDescriptors()
    {
        return rowDescriptors;
    }
}
