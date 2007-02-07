package com.zutubi.prototype.model;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class Form
{
    private List<Field> fields = new LinkedList<Field>();

    private String id;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void add(Field field)
    {
        this.fields.add(field);
    }

    public List<Field> getFields()
    {
        return fields;
    }

    public void setFields(List<Field> fields)
    {
        this.fields = fields;
    }
}
