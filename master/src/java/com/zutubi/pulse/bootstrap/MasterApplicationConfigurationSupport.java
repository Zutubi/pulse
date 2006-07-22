package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.bootstrap.conf.Config;
import com.zutubi.pulse.bootstrap.conf.ConfigSupport;
import com.opensymphony.util.TextUtils;

/**
 * <class-comment/>
 */
public class MasterApplicationConfigurationSupport extends ConfigSupport implements MasterApplicationConfiguration
{
    public MasterApplicationConfigurationSupport(Config config)
    {
        super(config);
    }

    public int getServerPort()
    {
        return getInteger(WEBAPP_PORT);
    }

    public String getAdminLogin()
    {
        return getProperty(ADMIN_LOGIN);
    }

    public void setAdminLogin(String login)
    {
        setProperty(ADMIN_LOGIN, login);
    }

    public String getBaseUrl()
    {
        return getProperty(BASE_URL);
    }

    public void setBaseUrl(String host)
    {
        // munge the url a little. We assume that there is no trailing '/' when using this property.
        if (TextUtils.stringSet(host) && host.endsWith("/"))
        {
            host = host.substring(0, host.length() - 1);
        }
        setProperty(BASE_URL, host);
    }

    public String getContextPath()
    {
        return getProperty(CONTEXT_PATH);
    }

    public void setContextPath(String path)
    {
        setProperty(CONTEXT_PATH, path);
    }

    public String getHelpUrl()
    {
        return getProperty(HELP_URL);
    }

    public void setHelpUrl(String helpUrl)
    {
        setProperty(HELP_URL, helpUrl);
    }

    public String getSmtpHost()
    {
        return getProperty(SMTP_HOST);
    }

    public void setSmtpHost(String host)
    {
        setProperty(SMTP_HOST, host);
    }

    public String getSmtpFrom()
    {
        return getProperty(SMTP_FROM);
    }

    public void setSmtpFrom(String from)
    {
        setProperty(SMTP_FROM, from);
    }

    public String getSmtpPassword()
    {
        return getProperty(SMTP_PASSWORD);
    }

    public void setSmtpPassword(String password)
    {
        setProperty(SMTP_PASSWORD, password);
    }

    public String getLogConfig()
    {
        return getProperty(LOGGING_CONFIG);
    }

    public void setLogConfig(String logConfig)
    {
        setProperty(LOGGING_CONFIG, logConfig);
    }

    public Boolean getJabberForceSSL()
    {
        return getBooleanProperty(JABBER_FORCE_SSL, Boolean.FALSE);
    }

    public void setJabberForceSSL(Boolean forceSSL)
    {
        setBooleanProperty(JABBER_FORCE_SSL, forceSSL);
    }

    public Boolean getRssEnabled()
    {
        return getBooleanProperty(RSS_ENABLED, Boolean.FALSE);
    }

    public void setRssEnabled(Boolean rssEnabled)
    {
        setBooleanProperty(RSS_ENABLED, rssEnabled);
    }

    public Boolean getAnonymousAccessEnabled()
    {
        return getBooleanProperty(ANONYMOUS_ACCESS_ENABLED, Boolean.FALSE);
    }

    public void setAnonymousAccessEnabled(Boolean anonEnabled)
    {
        setBooleanProperty(ANONYMOUS_ACCESS_ENABLED, anonEnabled);
    }

    public Boolean getLdapEnabled()
    {
        return getBooleanProperty(LDAP_ENABLED, false);
    }

    public void setLdapEnabled(Boolean enabled)
    {
        setBooleanProperty(LDAP_ENABLED, enabled);
    }

    public String getLdapHostUrl()
    {
        return getProperty(LDAP_HOST_URL);
    }

    public void setLdapHostUrl(String hostUrl)
    {
        setProperty(LDAP_HOST_URL, hostUrl);
    }

    public String getLdapBaseDn()
    {
        return getProperty(LDAP_BASE_DN);
    }

    public void setLdapBaseDn(String baseDn)
    {
        setProperty(LDAP_BASE_DN, baseDn);
    }

    public String getLdapManagerDn()
    {
        return getProperty(LDAP_MANAGER_DN);
    }

    public void setLdapManagerDn(String managerDn)
    {
        setProperty(LDAP_MANAGER_DN, managerDn);
    }

    public String getLdapManagerPassword()
    {
        return getProperty(LDAP_MANAGER_PASSWORD);
    }

    public void setLdapManagerPassword(String managerPassword)
    {
        setProperty(LDAP_MANAGER_PASSWORD, managerPassword);
    }

    public String getLdapUserFilter()
    {
        return getProperty(LDAP_USER_FILTER);
    }

    public void setLdapUserFilter(String userFilter)
    {
        setProperty(LDAP_USER_FILTER, userFilter);
    }

    public Boolean getLdapAutoAdd()
    {
        return getBooleanProperty(LDAP_AUTO_ADD, false);
    }

    public void setLdapAutoAdd(Boolean autoAdd)
    {
        setBooleanProperty(LDAP_AUTO_ADD, autoAdd);
    }

    public String getSmtpPrefix()
    {
        return getProperty(SMTP_PREFIX);
    }

    public void setSmtpPrefix(String prefix)
    {
        setProperty(SMTP_PREFIX, prefix);
    }

    public String getSmtpUsername()
    {
        return getProperty(SMTP_USERNAME);
    }

    public void setSmtpUsername(String username)
    {
        setProperty(SMTP_USERNAME, username);
    }

    public String getJabberHost()
    {
        return getProperty(JABBER_HOST);
    }

    public void setJabberHost(String host)
    {
        setProperty(JABBER_HOST, host);
    }

    public int getJabberPort()
    {
        return getInteger(JABBER_PORT, JABBER_PORT_DEFAULT);
    }

    public void setJabberPort(int port)
    {
        setInteger(JABBER_PORT, port);
    }

    public String getJabberUsername()
    {
        return getProperty(JABBER_USERNAME);
    }

    public void setJabberUsername(String username)
    {
        setProperty(JABBER_USERNAME, username);
    }

    public String getJabberPassword()
    {
        return getProperty(JABBER_PASSWORD);
    }

    public void setJabberPassword(String password)
    {
        setProperty(JABBER_PASSWORD, password);
    }
}
