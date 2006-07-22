package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.bootstrap.conf.Config;
import com.zutubi.pulse.bootstrap.conf.ConfigSupport;
import com.opensymphony.util.TextUtils;

/**
 * <class-comment/>
 */
public class MasterApplicationConfigurationSupport implements MasterApplicationConfiguration
{
    private final ConfigSupport config;

    public MasterApplicationConfigurationSupport(ConfigSupport config)
    {
        this.config = config;
    }

    public MasterApplicationConfigurationSupport(Config config)
    {
        this.config = new ConfigSupport(config);
    }

    public int getServerPort()
    {
        return config.getInteger(WEBAPP_PORT);
    }

    public String getAdminLogin()
    {
        return config.getProperty(ADMIN_LOGIN);
    }

    public void setAdminLogin(String login)
    {
        config.setProperty(ADMIN_LOGIN, login);
    }

    public String getBaseUrl()
    {
        return config.getProperty(BASE_URL);
    }

    public void setBaseUrl(String host)
    {
        // munge the url a little. We assume that there is no trailing '/' when using this property.
        if (TextUtils.stringSet(host) && host.endsWith("/"))
        {
            host = host.substring(0, host.length() - 1);
        }
        config.setProperty(BASE_URL, host);
    }

    public String getContextPath()
    {
        return config.getProperty(CONTEXT_PATH);
    }

    public void setContextPath(String path)
    {
        config.setProperty(CONTEXT_PATH, path);
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

    public Boolean getLdapEnabled()
    {
        return config.getBooleanProperty(LDAP_ENABLED, false);
    }

    public void setLdapEnabled(Boolean enabled)
    {
        config.setBooleanProperty(LDAP_ENABLED, enabled);
    }

    public String getLdapHostUrl()
    {
        return config.getProperty(LDAP_HOST_URL);
    }

    public void setLdapHostUrl(String hostUrl)
    {
        config.setProperty(LDAP_HOST_URL, hostUrl);
    }

    public String getLdapBaseDn()
    {
        return config.getProperty(LDAP_BASE_DN);
    }

    public void setLdapBaseDn(String baseDn)
    {
        config.setProperty(LDAP_BASE_DN, baseDn);
    }

    public String getLdapManagerDn()
    {
        return config.getProperty(LDAP_MANAGER_DN);
    }

    public void setLdapManagerDn(String managerDn)
    {
        config.setProperty(LDAP_MANAGER_DN, managerDn);
    }

    public String getLdapManagerPassword()
    {
        return config.getProperty(LDAP_MANAGER_PASSWORD);
    }

    public void setLdapManagerPassword(String managerPassword)
    {
        config.setProperty(LDAP_MANAGER_PASSWORD, managerPassword);
    }

    public String getLdapUserFilter()
    {
        return config.getProperty(LDAP_USER_FILTER);
    }

    public void setLdapUserFilter(String userFilter)
    {
        config.setProperty(LDAP_USER_FILTER, userFilter);
    }

    public Boolean getLdapAutoAdd()
    {
        return config.getBooleanProperty(LDAP_AUTO_ADD, false);
    }

    public void setLdapAutoAdd(Boolean autoAdd)
    {
        config.setBooleanProperty(LDAP_AUTO_ADD, autoAdd);
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
        return config.getInteger(JABBER_PORT, JABBER_PORT_DEFAULT);
    }

    public void setJabberPort(int port)
    {
        config.setInteger(JABBER_PORT, port);
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
