package com.zutubi.pulse.master.tove.model;

/**
 *
 *
 */
public class Field extends AbstractParameterised
{
    public Field(String type, String name)
    {
        setType(type);
        setName(name);
        setId("zfid." + name);
    }

    public String getName()
    {
        return (String) getParameter("name");
    }

    private Field setName(String name)
    {
        addParameter("name", name);
        return this;
    }

    public String getType()
    {
        return (String) getParameter("type");
    }

    private Field setType(String type)
    {
        addParameter("type", type);
        return this;
    }

    public String getLabel()
    {
        return (String) getParameter("label");
    }

    public Field setLabel(String label)
    {
        addParameter("label", label);
        return this;
    }

    public Object getValue()
    {
        return getParameter("value");
    }

    public Field setValue(Object value)
    {
        addParameter("value", value);
        return this;
    }

    public String getId()
    {
        return (String) getParameter("id");
    }

    private Field setId(String id)
    {
        addParameter("id", id);
        return this;
    }
}
