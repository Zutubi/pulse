package com.zutubi.prototype.model;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 *
 *
 */
public class UIComponent
{
    protected Map<String, Object> parameters = new HashMap<String, Object>();

    public void addParameter(String name, Object value)
    {
        parameters.put(name, value);
    }

    public void setParameters(Map<String, Object> parameters)
    {
        this.parameters.clear();
        this.parameters.putAll(parameters);
    }

    public void addAll(Map<String, Object> parameters)
    {
        this.parameters.putAll(parameters);
    }

    public Map<String, Object> getParameters()
    {
        return Collections.unmodifiableMap(parameters);
    }
}
