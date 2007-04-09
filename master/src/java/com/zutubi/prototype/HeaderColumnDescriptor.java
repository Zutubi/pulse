package com.zutubi.prototype;

import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.type.record.Record;

/**
 *
 *
 */
public class HeaderColumnDescriptor extends ColumnDescriptor
{
    private String name;

    private int colspan;

    public HeaderColumnDescriptor(String name)
    {
        this(name, 1);
    }

    public HeaderColumnDescriptor(String name, int colspan)
    {
        this.name = name;
        this.colspan = colspan;
    }

    public Column instantiate(String path, Record value)
    {
        Column column = new Column();
        column.addParameter("type", "header");
        column.setValue(name);
        column.setSpan(colspan);
        return column;
    }
}
