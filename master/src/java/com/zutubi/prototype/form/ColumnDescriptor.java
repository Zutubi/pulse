package com.zutubi.prototype.form;

import java.util.Map;

/**
 *
 *
 */
public class ColumnDescriptor implements Descriptor
{
    private String name;

    public void addParameter(String key, Object value)
    {
    }

    public Map<String, Object> getParameters()
    {
        return null;
    }

    public Object instantiate(Object obj)
    {
        return null;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
