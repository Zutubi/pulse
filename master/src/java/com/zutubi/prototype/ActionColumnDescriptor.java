package com.zutubi.prototype;

import com.zutubi.pulse.prototype.record.RecordPropertyInfo;

/**
 *
 *
 */
public class ActionColumnDescriptor extends ColumnDescriptor
{
    public ActionColumnDescriptor(String actionName, RecordPropertyInfo propertyInfo)
    {
        this(actionName, propertyInfo, 1);
    }
    
    public ActionColumnDescriptor(String actionName, RecordPropertyInfo propertyInfo, int colspan)
    {
        setName("actions");
        setColspan(colspan);
        getParameters().put("label", actionName);
        getParameters().put("type", "action");
    }
}
