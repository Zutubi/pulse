package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.util.bean.ObjectFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public class DelegateScmClientFactory implements ScmClientFactory<Configuration>
{
    private Map<String, Map<Object, Object>> dataCaches = new HashMap<String, Map<Object, Object>>();

    private ObjectFactory objectFactory;

    private Map<Class, ScmClientFactory<Configuration>> factories = new HashMap<Class, ScmClientFactory<Configuration>>();

    public ScmClient createClient(Configuration config) throws ScmException
    {
        ScmClientFactory<Configuration> factory = getFactory(config);
        ScmClient client = factory.createClient(config);
        if (client instanceof DataCacheAware)
        {
            DataCacheAware aware = (DataCacheAware) client;
            String key = aware.getCacheId();
            if (!dataCaches.containsKey(key))
            {
                dataCaches.put(key, new HashMap<Object, Object>());
            }
            Map<Object, Object> cache = dataCaches.get(key);
            aware.setCache(cache);
        }
        return client;
    }

    private ScmClientFactory<Configuration> getFactory(Object config)
    {
        return factories.get(config.getClass());
    }

    public void register(Class configType, Class<ScmClientFactory<Configuration>> factoryType) throws ScmException
    {
        try
        {
            ScmClientFactory<Configuration> factory = objectFactory.buildBean(factoryType);
            factories.put(configType, factory);
        }
        catch (Exception e)
        {
            throw new ScmException(e);
        }
    }

    public void unregister(Class configType)
    {
        factories.remove(configType);
    }

    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}
