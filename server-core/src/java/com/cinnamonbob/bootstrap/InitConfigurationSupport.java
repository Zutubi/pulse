package com.cinnamonbob.bootstrap;

import com.cinnamonbob.bootstrap.config.Configuration;
import com.cinnamonbob.bootstrap.config.ConfigurationSupport;

import java.io.File;

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

    public File getBobHome()
    {
        return config.getFile(BOB_HOME);
    }

    public void setBobHome(File home)
    {
        config.setFile(BOB_HOME, home);
    }
}
