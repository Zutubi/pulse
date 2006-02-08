package com.cinnamonbob.bootstrap;

import com.cinnamonbob.bootstrap.config.Configuration;
import com.cinnamonbob.bootstrap.config.ConfigurationSupport;

/**
 * <class-comment/>
 */
public class InitConfigurationSupport implements InitConfiguration
{
    private final ConfigurationSupport config;

    public InitConfigurationSupport(Configuration config)
    {
        this.config = new ConfigurationSupport(config);
    }

    public String getBobHome()
    {
        return config.getProperty(BOB_HOME);
    }

    public void setBobHome(String home)
    {
        config.setProperty(BOB_HOME, home);
    }
}
