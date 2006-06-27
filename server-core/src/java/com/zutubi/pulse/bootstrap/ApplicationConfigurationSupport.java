package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.bootstrap.conf.Config;
import com.zutubi.pulse.bootstrap.conf.ConfigSupport;

/**
 * <class-comment/>
 */
public class ApplicationConfigurationSupport implements ApplicationConfiguration
{
    private final ConfigSupport config;

    public ApplicationConfigurationSupport(ConfigSupport config)
    {
        this.config = config;
    }

    public ApplicationConfigurationSupport(Config config)
    {
        this.config = new ConfigSupport(config);
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

    public Boolean getJabberForceSSL()
    {
        return config.getBooleanProperty(JABBER_FORCE_SSL, Boolean.FALSE);
    }

    public void setJabberForceSSL(Boolean forceSSL)
    {
        config.setBooleanProperty(JABBER_FORCE_SSL, forceSSL);
    }

    public Boolean getRssEnabled()
    {
        return config.getBooleanProperty(RSS_ENABLED, Boolean.FALSE);
    }

    public void setRssEnabled(Boolean rssEnabled)
    {
        config.setBooleanProperty(RSS_ENABLED, rssEnabled);
    }

    public Boolean getAnonymousAccessEnabled()
    {
        return config.getBooleanProperty(ANONYMOUS_ACCESS_ENABLED, Boolean.FALSE);
    }

    public void setAnonymousAccessEnabled(Boolean anonEnabled)
    {
        config.setBooleanProperty(ANONYMOUS_ACCESS_ENABLED, anonEnabled);
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

    public String getJabberHost()
    {
        return config.getProperty(JABBER_HOST);
    }

    public void setJabberHost(String host)
    {
        config.setProperty(JABBER_HOST, host);
    }

    public int getJabberPort()
    {
        return config.getInt(JABBER_PORT, JABBER_PORT_DEFAULT);
    }

    public void setJabberPort(int port)
    {
        config.setInt(JABBER_PORT, port);
    }

    public String getJabberUsername()
    {
        return config.getProperty(JABBER_USERNAME);
    }

    public void setJabberUsername(String username)
    {
        config.setProperty(JABBER_USERNAME, username);
    }

    public String getJabberPassword()
    {
        return config.getProperty(JABBER_PASSWORD);
    }

    public void setJabberPassword(String password)
    {
        config.setProperty(JABBER_PASSWORD, password);
    }
}
