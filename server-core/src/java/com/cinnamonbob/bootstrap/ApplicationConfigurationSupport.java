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

    public void setHostName(String host)
    {
        config.setProperty(HOST_NAME, host);
    }

    public String getHelpUrl()
    {
        return config.getProperty(HELP_URL);
    }

    public void setHelpUrl(String helpUrl)
    {
        config.setProperty(HELP_URL, helpUrl);
    }

    public String getSmtpHost()
    {
        return config.getProperty(SMTP_HOST);
    }

    public void setSmtpHost(String host)
    {
        config.setProperty(SMTP_HOST, host);
    }

    public String getSmtpFrom()
    {
        return config.getProperty(SMTP_FROM);
    }

    public void setSmtpFrom(String from)
    {
        config.setProperty(SMTP_FROM, from);
    }

    public String getSmtpPassword()
    {
        return config.getProperty(SMTP_PASSWORD);
    }

    public void setSmtpPassword(String password)
    {
        config.setProperty(SMTP_PASSWORD, password);
    }

    public String getLogConfig()
    {
        return config.getProperty(LOGGING_CONFIG);
    }

    public void setLogConfig(String logConfig)
    {
        config.setProperty(LOGGING_CONFIG, logConfig);
    }

    public String getSmtpPrefix()
    {
        return config.getProperty(SMTP_PREFIX);
    }

    public void setSmtpPrefix(String prefix)
    {
        config.setProperty(SMTP_PREFIX, prefix);
    }

    public String getSmtpUsername()
    {
        return config.getProperty(SMTP_USERNAME);
    }

    public void setSmtpUsername(String username)
    {
        config.setProperty(SMTP_USERNAME, username);
    }
}
