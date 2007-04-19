package com.zutubi.pulse.core;

import com.zutubi.pulse.core.model.Property;
import com.zutubi.util.bean.ObjectFactory;

import java.util.Map;
import java.util.HashMap;

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
    }

    public PulseFileLoader createLoader()
    {
        PulseFileLoader loader = new PulseFileLoader();
        loader.setObjectFactory(objectFactory);
        for(Map.Entry<String, Class> entry: types.entrySet())
        {
            loader.register(entry.getKey(), entry.getValue());
        }

        return loader;
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
