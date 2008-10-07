package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.util.bean.ObjectFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of the ScmClientFactory that delegates the creation of the clients to the
 * ScmClientFactory classes registered with the configuration.  This factory also handles the
 * data cache for the various client instances. 
 *
 * 
 */
public class DelegateScmClientFactory implements ScmClientFactory<com.zutubi.pulse.core.scm.config.ScmConfiguration>
{
    private Map<String, Map<Object, Object>> dataCaches = new HashMap<String, Map<Object, Object>>();

    private ObjectFactory objectFactory;

    private Map<Class, ScmClientFactory<com.zutubi.pulse.core.scm.config.ScmConfiguration>> factories = new HashMap<Class, ScmClientFactory<com.zutubi.pulse.core.scm.config.ScmConfiguration>>();

    public ScmClient createClient(com.zutubi.pulse.core.scm.config.ScmConfiguration config) throws ScmException
    {
        ScmClientFactory<com.zutubi.pulse.core.scm.config.ScmConfiguration> factory = getFactory(config);
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

    private ScmClientFactory<com.zutubi.pulse.core.scm.config.ScmConfiguration> getFactory(Object config)
    {
        return factories.get(config.getClass());
    }

    public void register(Class configType, Class<? extends ScmClientFactory> factoryType) throws ScmException
    {
        try
        {
            ScmClientFactory<com.zutubi.pulse.core.scm.config.ScmConfiguration> factory = objectFactory.buildBean(factoryType);
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
