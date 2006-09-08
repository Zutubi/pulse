package com.zutubi.pulse.form.persistence;

import java.util.Map;
import java.util.HashMap;

/**
 * <class-comment/>
 */
public class DefaultObjectRegistry implements ObjectRegistry
{
    private Map<String, Class> registeredTypes = new HashMap<String, Class>();

    public Class findType(String key)
    {
        return registeredTypes.get(key);
    }

    public void register(String key, Class type)
    {
        registeredTypes.put(key, type);
    }

    public void unregister(String key)
    {
        registeredTypes.remove(key);
    }
}
