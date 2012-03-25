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
    private ConfigurationVariableProvider configurationVariableProvider;
    private ScmExtensionManager scmExtensionManager;
    
    public ScmClient createClient(ScmConfiguration config) throws ScmException
    {
        if (configurationVariableProvider != null)
        {
            config = configurationVariableProvider.resolveStringProperties(config);
        }
        
        ScmClientFactory factory = scmExtensionManager.getClientFactory(config);
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

    public void setConfigurationVariableProvider(ConfigurationVariableProvider configurationVariableProvider)
    {
        this.configurationVariableProvider = configurationVariableProvider;
    }

    public void setScmExtensionManager(ScmExtensionManager scmExtensionManager)
    {
        this.scmExtensionManager = scmExtensionManager;
    }
}
