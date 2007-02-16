package com.zutubi.prototype.model;

import java.util.Map;
import java.util.HashMap;

/**
 *
 *
 */
public class Column
{
    private String name;

    private int span = 1;

    private Object value;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public int getSpan()
    {
        return span;
    }

    public void setSpan(int span)
    {
        this.span = span;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public void addParameter(String key, Object value)
    {
        parameters.put(key, value);
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters)
    {
        this.parameters = new HashMap<String, Object>(parameters);
    }
}
