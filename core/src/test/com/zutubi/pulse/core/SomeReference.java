package com.zutubi.pulse.core;

/**
 * 
 *
 */
public class SomeReference implements Reference
{
    private String name;
    private String someValue;

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return this;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSomeValue()
    {
        return someValue;
    }

    public void setSomeValue(String someValue)
    {
        this.someValue = someValue;
    }
}
