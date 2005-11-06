package com.cinnamonbob.core;


/**
 * 
 *
 */
public class Property implements Reference
{
    private String name;
    private String value;

    public Property()
    {

    }

    public Property(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return value;
    }
}
