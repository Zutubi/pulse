package com.zutubi.pulse.master.tove.model;

/**
 * A single field in a form. Fields have a few common properties defined directly, plus additional
 * type-specific ones defined in the parameters map.
 */
public class Field extends AbstractParameterised
{
    private final String type;
    private final String name;
    private final String id;
    private String label;
    private Object value;

    public Field(String type, String name)
    {
        this.type = type;
        this.name = name;
        this.id = "zfid." + name;
    }

    public String getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public String getId()
    {
        return id;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }
}
