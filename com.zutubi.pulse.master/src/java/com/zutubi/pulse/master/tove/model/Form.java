package com.zutubi.pulse.master.tove.model;

import com.zutubi.tove.annotations.FieldType;
import flexjson.JSON;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class Form extends AbstractParameterised
{
    private final String name;
    private final String id;
    private String action;
    private String defaultSubmit;
    private boolean displayMode;
    private boolean readOnly;
    private boolean ajax;
    private boolean fileUpload;

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
        this.name = name;
        this.id = id;
        this.action = action;
        this.defaultSubmit = defaultSubmit;
    }

    public String getName()
    {
        return name;
    }

    public String getId()
    {
        return id;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getDefaultSubmit()
    {
        return defaultSubmit;
    }

    public void setDefaultSubmit(String defaultSubmit)
    {
        this.defaultSubmit = defaultSubmit;
    }

    public boolean isDisplayMode()
    {
        return displayMode;
    }

    public void setDisplayMode(boolean displayMode)
    {
        this.displayMode = displayMode;
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
    }

    public boolean isAjax()
    {
        return ajax;
    }

    public void setAjax(boolean ajax)
    {
        this.ajax = ajax;
    }

    public boolean isFileUpload()
    {
        return fileUpload;
    }

    public void setFileUpload(boolean fileUpload)
    {
        this.fileUpload = fileUpload;
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

    @JSON
    public List<Field> getFields()
    {
        return Collections.unmodifiableList(fields);
    }

    @JSON
    public List<Field> getSubmitFields()
    {
        return Collections.unmodifiableList(submitFields);
    }
}
