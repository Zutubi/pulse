package com.cinnamonbob.bootstrap;

/**
 * 
 *
 */
public class DefaultConfigurationManager implements ConfigurationManager
{
    private BootstrapManager bootstrapManager;

    public void setBootstrapManager(BootstrapManager bootstrapManager)
    {
        this.bootstrapManager = bootstrapManager;
    }

    public Config getAppConfig()
    {
        return new Config()
        {
            public int getServerPort()
            {
                return 8080;
            }
        };
    }
}
