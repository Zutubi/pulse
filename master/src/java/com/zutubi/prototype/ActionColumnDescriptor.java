package com.zutubi.prototype;

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
}
