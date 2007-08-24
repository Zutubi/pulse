package com.zutubi.pulse.webwork.mapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class ActionResolverSupport implements ActionResolver
{
    private String action;
    private Map<String, String> parameters = null;

    public ActionResolverSupport(String action)
    {
        this.action = action;
    }

    public String getAction()
    {
        return action;
    }

    public Map<String, String> getParameters()
    {
        if (parameters == null)
        {
            return Collections.EMPTY_MAP;
        }
        else
        {
            return parameters;
        }
    }

    protected void addParameter(String name, String value)
    {
        if(parameters == null)
        {
            parameters = new HashMap<String, String>();
        }
        parameters.put(name, value);
    }

    public ActionResolver getChild(String name)
    {
        return null;
    }
}
