package com.zutubi.pulse.acceptance.utils;

import com.zutubi.pulse.acceptance.rpc.RemoteApiClient;

/**
 * Implementation of the ConfigurationHelperFactory interface that creates a single
 * instance and reuses until this class is unloaded.
 *
 * One word of caution.  Because the xmlRpcHelper within the configuration helper is
 * updated during each create, the instances are not considered thread safe.
 */
public class SingletonConfigurationHelperFactory implements ConfigurationHelperFactory
{
    private static ConfigurationHelper INSTANCE;
    private static final Object lock = new Object();

    public ConfigurationHelper create(RemoteApiClient helper) throws Exception
    {
        synchronized (lock)
        {
            if (INSTANCE == null)
            {
                INSTANCE = new ConfigurationHelper();
                INSTANCE.setXmlRpcHelper(helper);
                INSTANCE.init();
            }
            else
            {
                INSTANCE.setXmlRpcHelper(helper);
            }
            return INSTANCE;
        }
    }
}
