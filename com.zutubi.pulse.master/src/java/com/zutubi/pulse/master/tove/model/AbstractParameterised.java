package com.zutubi.pulse.master.tove.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public abstract class AbstractParameterised implements Parameterised
{
    private Map<String, Object> parameters = new HashMap<String, Object>();

    public void addParameter(String key, Object value)
    {
        parameters.put(key, value);
    }

    public void addAll(Map<String, Object> parameters)
    {
        this.parameters.putAll(parameters);
    }

    public Map<String, Object> getParameters()
    {
        return this.parameters;
    }

    public boolean hasParameter(String key)
    {
        return parameters.containsKey(key);
    }

    public Object getParameter(String key)
    {
        return this.parameters.get(key);
    }

    public <T> T getParameter(String key, T defaultValue)
    {
        T value = (T) parameters.get(key);
        if(value == null)
        {
            value = defaultValue;
        }
        return value;
    }
}
