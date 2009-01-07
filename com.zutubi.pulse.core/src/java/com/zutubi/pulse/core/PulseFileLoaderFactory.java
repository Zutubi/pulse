package com.zutubi.pulse.core;

import com.zutubi.util.bean.ObjectFactory;
import com.zutubi.pulse.core.engine.api.Property;

import java.util.HashMap;
import java.util.Map;

/**
 * A factory for creating PulseFileLoader objects that are aware of the
 * current plugins.
 */
public class PulseFileLoaderFactory
{
    private Map<String, Class> types = new HashMap<String, Class>();
    private ObjectFactory objectFactory;

    public PulseFileLoaderFactory()
    {
        register("property", Property.class);
        register("recipe", Recipe.class);
        register("def", ComponentDefinition.class);
        register("register", Register.class);
        register("version", Version.class);
    }

    public PulseFileLoader createLoader()
    {
        try
        {
            PulseFileLoader loader = objectFactory.buildBean(PulseFileLoader.class);
            for(Map.Entry<String, Class> entry: types.entrySet())
            {
                loader.register(entry.getKey(), entry.getValue());
            }
            return loader;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create file loader instance. Cause: " + e.getMessage(), e);
        }
    }

    public void register(String name, Class type)
    {
        types.put(name, type);
    }

    public void unregister(String name)
    {
        types.remove(name);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
