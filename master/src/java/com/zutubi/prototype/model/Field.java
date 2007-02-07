package com.zutubi.prototype.model;

import java.util.Map;
import java.util.HashMap;

/**
 *
 *
 */
public class Field
{
    private int tabindex;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    public int getTabindex()
    {
        return tabindex;
    }

    public void setTabindex(int tabindex)
    {
        this.tabindex = tabindex;
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
        this.parameters = parameters;
    }
}
