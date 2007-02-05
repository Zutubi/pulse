package com.zutubi.prototype.form;

import com.zutubi.pulse.prototype.record.RecordPropertyInfo;

/**
 *
 *
 */
public class ActionColumnDescriptor extends ColumnDescriptor
{
    public ActionColumnDescriptor(String actionName, RecordPropertyInfo propertyInfo)
    {
        setName("actions");
        getParameters().put("label", actionName);
        getParameters().put("type", "action");
    }
}
