package com.zutubi.pulse.master.webwork.dispatcher.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper base class that implements the ActionResolver interface.
 */
public abstract class ActionResolverSupport implements ActionResolver
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
            return Collections.emptyMap();
        }
        else
        {
            return parameters;
        }
    }

    public void addParameter(String name, String value)
    {
        if(parameters == null)
        {
            parameters = new HashMap<String, String>();
        }
        parameters.put(name, value);
    }

    public List<String> listChildren()
    {
        return Collections.EMPTY_LIST;
    }

    public ActionResolver getChild(String name)
    {
        return null;
    }
}
