package com.zutubi.pulse.form.persistence.stores;

import com.zutubi.pulse.form.persistence.Copyable;
import com.zutubi.pulse.form.persistence.ObjectRegistry;
import com.zutubi.pulse.form.persistence.ObjectStore;

import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class MemoryObjectStore implements ObjectStore
{
    private ObjectRegistry registry;

    private Map<String, Copyable> instances = new HashMap<String, Copyable>();

    public Copyable reset(String key)
    {
        instances.put(key, (Copyable) createInstance(key));
        return instances.get(key).copy();
    }

    public Copyable load(String key)
    {
        if (!instances.containsKey(key))
        {
            instances.put(key, (Copyable) createInstance(key));
        }
        return instances.get(key).copy();
    }

    public void save(String key, Copyable obj)
    {
        instances.put(key, obj.copy());
    }

    private Object createInstance(String key)
    {
        Class cls = registry.findType(key);
        try
        {
            return cls.newInstance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void setObjectRegistry(ObjectRegistry registry)
    {
        this.registry = registry;
    }
}
