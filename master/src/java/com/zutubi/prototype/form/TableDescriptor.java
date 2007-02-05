package com.zutubi.prototype.form;

import com.zutubi.prototype.form.model.Table;
import com.zutubi.prototype.form.model.Column;
import com.zutubi.prototype.form.model.Row;

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

        Column previousColumn = null;
        for (ColumnDescriptor columnDescriptor : columnDescriptors)
        {
            if (previousColumn  != null && previousColumn.getName().equals(columnDescriptor.getName()))
            {
                previousColumn.setSpan(previousColumn.getSpan() + 1);
            }
            else
            {
                Column column = new Column();
                column.setName(columnDescriptor.getName());
                table.addColumn(column);
                previousColumn = column;
            }
        }

        List<Object> list = (List<Object>) obj;
        for (int i = 0; i < list.size(); i++)
        {
            Object item = list.get(i);
            Row row = new Row();
            row.setIndex(i);
            for (ColumnDescriptor c : columnDescriptors)
            {
                row.addCell((Column) c.instantiate(i, item));
            }
            table.addRow(row);
        }

/*
        // this guy should not be making this decision.

        Row addItemRow = new Row();
        addItemRow.setIndex(list.size());
        table.addRow(addItemRow);

        Column addItemCell = new Column();
        addItemCell.setSpan(columnDescriptors.size());
        addItemCell.setValue("<a href=\"addEntry.action?path=&projectId=\">add.item</a>"); //TODO: i18n
        addItemRow.addCell(addItemCell);
*/

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
}
