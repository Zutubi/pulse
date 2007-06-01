package com.zutubi.prototype.model;

/**
 *
 *
 */
public class Field extends UIComponent
{
    public String getName()
    {
        return (String) parameters.get("name");
    }

    public Field setName(String name)
    {
        parameters.put("name", name);
        return this;
    }

    public String getType()
    {
        return (String) parameters.get("type");
    }

    public Field setType(String type)
    {
        parameters.put("type", type);
        return this;
    }

    public String getLabel()
    {
        return (String)parameters.get("label");
    }

    public Field setLabel(String label)
    {
        parameters.put("label", label);
        return this;
    }

    public Object getValue()
    {
        return parameters.get("value");
    }

    public Field setValue(Object value)
    {
        parameters.put("value", value);
        return this;
    }

    public String getId()
    {
        return (String) parameters.get("id");
    }

    public Field setId(String id)
    {
        parameters.put("id", id);
        return this;
    }
}
