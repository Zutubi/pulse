package com.zutubi.pulse.master.tove.model;

import com.zutubi.tove.annotations.FieldType;

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
    private static final String PARAMETER_DEFAULT_SUBMIT = "defaultSubmit";

    /**
     * Ordered list of fields that make up this form.
     */
    private List<Field> fields = new LinkedList<Field>();
    /**
     * The buttons used to submit this form.
     */
    private List<Field> submitFields = new LinkedList<Field>();

    public Form(String name, String id, String action, String defaultSubmit)
    {
        setName(name);
        setId(id);
        setAction(action);
        setDefaultSubmit(defaultSubmit);
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

    public String getDefaultSubmit()
    {
        return (String) getParameter(PARAMETER_DEFAULT_SUBMIT);
    }

    public void setDefaultSubmit(String defaultSubmit)
    {
        addParameter(PARAMETER_DEFAULT_SUBMIT, defaultSubmit);
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
