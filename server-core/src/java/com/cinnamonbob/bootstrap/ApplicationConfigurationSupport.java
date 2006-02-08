package com.cinnamonbob.bootstrap;

import com.cinnamonbob.bootstrap.config.Configuration;
import com.cinnamonbob.bootstrap.config.ConfigurationSupport;

/**
 * <class-comment/>
 */
public class ApplicationConfigurationSupport implements ApplicationConfiguration
{
    private final ConfigurationSupport config;

    public ApplicationConfigurationSupport(ConfigurationSupport config)
    {
        this.config = config;
    }

    public ApplicationConfigurationSupport(Configuration config)
    {
        this.config = new ConfigurationSupport(config);
    }

    public int getAdminPort()
    {
        return config.getInt(ADMIN_PORT);
    }

    public int getServerPort()
    {
        return config.getInt(WEBAPP_PORT);
    }

    public String getHostName()
    {
        return config.getProperty(HOST_NAME);
    }

    public String getSmtpHost()
    {
        return config.getProperty(SMTP_HOST);
    }

    public String getSmtpFrom()
    {
        return config.getProperty(SMTP_FROM);
    }
}
