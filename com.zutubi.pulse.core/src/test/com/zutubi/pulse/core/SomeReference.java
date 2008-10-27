package com.zutubi.pulse.core;

import com.zutubi.pulse.core.engine.api.Reference;

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
