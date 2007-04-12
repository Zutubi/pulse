package com.zutubi.prototype.model;

import com.zutubi.prototype.webwork.PrototypeUtils;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

/**
 *
 *
 */
public class Form extends UIComponent
{
    /**
     * Ordered list of fields that make up this form.
     */
    private List<Field> fields = new LinkedList<Field>();

    /**
     * The submit fields
     */
    private List<Field> submitFields = new LinkedList<Field>();

    public Form()
    {
    }

    public String getId()
    {
        return (String) parameters.get("id");
    }

    public void setId(String id)
    {
        parameters.put("id", id);
    }

    public String getAction()
    {
        return (String) parameters.get("action");
    }

    public void setAction(String action)
    {
        parameters.put("action", action);
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
