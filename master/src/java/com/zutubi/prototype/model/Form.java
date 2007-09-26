package com.zutubi.prototype.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class Form extends UIComponent
{
    private static final String PARAMETER_NAME = "name";
    private static final String PARAMETER_ID = "id";
    private static final String PARAMETER_ACTION = "action";
    private static final String PARAMETER_DISPLAY_MODE = "displayMode";
    private static final String PARAMETER_READ_ONLY = "readOnly";
    private static final String PARAMETER_AJAX = "ajax";

    /**
     * Ordered list of fields that make up this form.
     */
    private List<Field> fields = new LinkedList<Field>();
    /**
     * The buttons used to submit this form.
     */
    private List<Field> submitFields = new LinkedList<Field>();

    public Form()
    {
        parameters.put(PARAMETER_AJAX, true);
        parameters.put(PARAMETER_DISPLAY_MODE, false);
    }

    public String getName()
    {
        return (String) parameters.get(PARAMETER_NAME);
    }

    public void setName(String name)
    {
        parameters.put(PARAMETER_NAME, name);
    }

    public String getId()
    {
        return (String) parameters.get(PARAMETER_ID);
    }

    public void setId(String id)
    {
        parameters.put(PARAMETER_ID, id);
    }

    public String getAction()
    {
        return (String) parameters.get(PARAMETER_ACTION);
    }

    public void setAction(String action)
    {
        parameters.put(PARAMETER_ACTION, action);
    }

    public boolean getDisplayMode()
    {
        return (Boolean) parameters.get(PARAMETER_DISPLAY_MODE);
    }

    public void setDisplayMode(boolean displayMode)
    {
        parameters.put(PARAMETER_DISPLAY_MODE, displayMode);
    }

    public boolean getReadOnly()
    {
        return (Boolean) parameters.get(PARAMETER_READ_ONLY);
    }

    public void setReadOnly(boolean readOnly)
    {
        parameters.put(PARAMETER_READ_ONLY, readOnly);
    }

    public boolean getAjax()
    {
        return (Boolean) parameters.get(PARAMETER_AJAX);
    }

    public void setAjax(boolean ajax)
    {
        parameters.put(PARAMETER_AJAX, ajax);
    }

    public void add(Field field)
    {
        if ("submit".equals(field.getType()))
        {
            this.submitFields.add(field);
        }
        else
        {
            this.fields.add(field);
        }
    }

    public List<Field> getFields()
    {
        return Collections.unmodifiableList(fields);
    }

    public void setFields(List<Field> fields)
    {
        this.fields.clear();
        this.submitFields.clear();

        for (Field field : fields)
        {
            add(field);
        }
    }

    public List<Field> getSubmitFields()
    {
        return Collections.unmodifiableList(submitFields);
    }
}
