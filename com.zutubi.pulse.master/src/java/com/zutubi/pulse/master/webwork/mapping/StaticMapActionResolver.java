package com.zutubi.pulse.master.webwork.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 */
public abstract class StaticMapActionResolver extends ActionResolverSupport
{
    private Map<String, ActionResolver> map = new HashMap<String, ActionResolver>();

    public StaticMapActionResolver(String action)
    {
        super(action);
    }

    protected void addMapping(String name, ActionResolver child)
    {
        map.put(name, child);
    }

    public ActionResolver getChild(String name)
    {
        return map.get(name);
    }
}
