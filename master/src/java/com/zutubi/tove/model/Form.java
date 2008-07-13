package com.zutubi.tove.model;

import com.zutubi.config.annotations.FieldType;
import com.zutubi.tove.AbstractParameterised;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class Form extends AbstractParameterised
{
    private static final String PARAMETER_NAME = "name";
    private static final String PARAMETER_ID = "id";
    private static final String PARAMETER_ACTION = "action";
    private static final String PARAMETER_DISPLAY_MODE = "displayMode";
    private static final String PARAMETER_READ_ONLY = "readOnly";
    private static final String PARAMETER_AJAX = "ajax";
    private static final String PARAMETER_FILE_UPLOAD = "fileUpload";

    /**
     * Ordered list of fields that make up this form.
     */
    private List<Field> fields = new LinkedList<Field>();
    /**
     * The buttons used to submit this form.
     */
    private List<Field> submitFields = new LinkedList<Field>();

    public Form(String name, String id, String action)
    {
        setName(name);
        setId(id);
        setAction(action);
    }

    public String getName()
    {
        return (String) getParameter(PARAMETER_NAME);
    }

    private void setName(String name)
    {
        addParameter(PARAMETER_NAME, name);
    }

    public String getId()
    {
        return (String) getParameter(PARAMETER_ID);
    }

    private void setId(String id)
    {
        addParameter(PARAMETER_ID, id);
    }

    public String getAction()
    {
        return (String) getParameter(PARAMETER_ACTION);
    }

    public void setAction(String action)
    {
        addParameter(PARAMETER_ACTION, action);
    }

    public boolean isDisplayMode()
    {
        return getParameter(PARAMETER_DISPLAY_MODE, false);
    }

    public void setDisplayMode(boolean displayMode)
    {
        addParameter(PARAMETER_DISPLAY_MODE, displayMode);
    }

    public boolean isReadOnly()
    {
        return getParameter(PARAMETER_READ_ONLY, false);
    }

    public void setReadOnly(boolean readOnly)
    {
        addParameter(PARAMETER_READ_ONLY, readOnly);
    }

    public boolean isAjax()
    {
        return getParameter(PARAMETER_AJAX, true);
    }

    public void setAjax(boolean ajax)
    {
        addParameter(PARAMETER_AJAX, ajax);
    }

    public boolean isFileUpload()
    {
        return getParameter(PARAMETER_FILE_UPLOAD, false);
    }

    public void setFileUpload(boolean fileUpload)
    {
        addParameter(PARAMETER_FILE_UPLOAD, fileUpload);
    }

    public void add(Field field)
    {
        if (FieldType.SUBMIT.equals(field.getType()))
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

    public List<Field> getSubmitFields()
    {
        return Collections.unmodifiableList(submitFields);
    }
}
