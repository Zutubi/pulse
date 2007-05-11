package com.zutubi.prototype;

import com.zutubi.prototype.model.Column;
import com.zutubi.prototype.type.record.Record;

/**
 */
public class ActionColumnDescriptor extends ColumnDescriptor
{
    private static final String PARAMETER_AJAX = "ajax";

    public ActionColumnDescriptor(String actionName, boolean ajax)
    {
        this(actionName, 1, ajax);
    }

    public ActionColumnDescriptor(String actionName, int colspan, boolean ajax)
    {
        setName("actions");
        setAjax(ajax);
        setColspan(colspan);
        getParameters().put("value", actionName);
        getParameters().put("type", "action");
    }

    public Column instantiate(String path, Record value)
    {
        Column column = new Column();
        column.addAll(getParameters());
        column.setSpan(colspan);
        return column;
    }

    public boolean isAjax()
    {
        return (Boolean) getParameters().get(PARAMETER_AJAX);
    }

    private void setAjax(boolean ajax)
    {
        getParameters().put(PARAMETER_AJAX, ajax);
    }
}
