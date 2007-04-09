package com.zutubi.prototype;

import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.type.record.Record;

/**
 *
 *
 */
public class ActionColumnDescriptor extends ColumnDescriptor
{
    public ActionColumnDescriptor(String actionName)
    {
        this(actionName, 1);
    }
    
    public ActionColumnDescriptor(String actionName, int colspan)
    {
        setName("actions");
        setColspan(colspan);
        getParameters().put("label", actionName);
        getParameters().put("type", "action");
    }

    public Column instantiate(String path, Record value)
    {
        Column column = new Column();
        column.addAll(getParameters());
        column.setSpan(colspan);
        return column;

    }
}
