package com.zutubi.pulse.master.webwork.dispatcher.mapper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A resolver that is based on a fixed mapping of child name to child resolver.
 * Used for paths where all children are known statically.
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

    public List<String> listChildren()
    {
        return new LinkedList<String>(map.keySet());
    }

    public ActionResolver getChild(String name)
    {
        return map.get(name);
    }
}
