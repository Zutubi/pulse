package com.zutubi.pulse.prototype;

import com.zutubi.prototype.Path;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 *
 */
public class PrototypeConfigRegistry
{
    // Mapping -> scopeName : {rootProperty:symbolicName}
    private Map<String, Map<String, String>> scopeDefs = new HashMap<String, Map<String, String>>();

    public Map<String, String> addScope(String scope)
    {
        scopeDefs.put(scope, new HashMap<String, String>());
        return scopeDefs.get(scope);
    }

    public List<String> getScopes()
    {
        return new LinkedList<String>(scopeDefs.keySet());
    }

    public List<String> getRoot(String scope)
    {
        if (scopeDefs.containsKey(scope))
        {
            return new LinkedList<String>(scopeDefs.get(scope).keySet());
        }
        return null;
    }

    public Map<String, String> getScope(String s)
    {
        return scopeDefs.get(s);
    }

    public Object get(Path path)
    {
        return null;
    }
}
