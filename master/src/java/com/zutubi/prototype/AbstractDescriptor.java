package com.zutubi.prototype;

import java.util.Map;
import java.util.HashMap;

/**
 *
 *
 */
public abstract class AbstractDescriptor implements Descriptor
{
    protected Map<String, Object> parameters = new HashMap<String, Object>();

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
}
