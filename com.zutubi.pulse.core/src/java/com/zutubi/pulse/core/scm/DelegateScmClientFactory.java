package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmClientFactory;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.tove.variables.ConfigurationVariableProvider;
import com.zutubi.util.bean.ObjectFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of the ScmClientFactory that delegates the creation of the clients to the
 * ScmClientFactory classes registered with the configuration.  This factory also handles the
 * data cache for the various client instances. 
 */
public class DelegateScmClientFactory implements ScmClientFactory<ScmConfiguration>
{
    private Map<String, Map<Object, Object>> dataCaches = new HashMap<String, Map<Object, Object>>();
    private Map<Class, ScmClientFactory> factories = new HashMap<Class, ScmClientFactory>();
    private ObjectFactory objectFactory;
    private ConfigurationVariableProvider configurationVariableProvider;
    
    public ScmClient createClient(ScmConfiguration config) throws ScmException
    {
        if (configurationVariableProvider != null)
        {
            config = configurationVariableProvider.resolveStringProperties(config);
        }
        
        ScmClientFactory factory = getFactory(config);
        @SuppressWarnings({"unchecked"})
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

    private ScmClientFactory getFactory(ScmConfiguration config)
    {
        return factories.get(config.getClass());
    }

    public <T extends ScmConfiguration> void register(Class<T> configType, Class<? extends ScmClientFactory<T>> factoryType) throws ScmException
    {
        try
        {
            ScmClientFactory<T> factory = objectFactory.buildBean(factoryType);
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

    public void setConfigurationVariableProvider(ConfigurationVariableProvider configurationVariableProvider)
    {
        this.configurationVariableProvider = configurationVariableProvider;
    }
}
