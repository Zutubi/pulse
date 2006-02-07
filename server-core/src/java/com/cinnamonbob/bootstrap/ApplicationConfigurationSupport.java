package com.cinnamonbob.bootstrap;

/**
 * <class-comment/>
 */
public class ApplicationConfigurationSupport implements ApplicationConfiguration
{
    //---( server configuration )---
    private static final String ADMIN_PORT = "admin.port";

    private static final String WEBAPP_PORT = "webapp.port";

    private static final String HOST_NAME = "host.name";

    //---( mail configuration )---

    private static final String SMTP_HOST = "mail.smtp.host";

    private static final String SMTP_FROM = "mail.smtp.from";

    private final ConfigurationSupport config;

    public ApplicationConfigurationSupport(ConfigurationSupport config)
    {
        this.config = config;
    }

    public ApplicationConfigurationSupport(Configuration... configs)
    {
        this.config = new ConfigurationSupport(configs);
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
