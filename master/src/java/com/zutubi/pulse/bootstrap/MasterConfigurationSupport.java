package com.zutubi.pulse.bootstrap;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.config.CompositeConfig;
import com.zutubi.pulse.config.Config;
import com.zutubi.pulse.config.ConfigSupport;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * <class-comment/>
 */
public class MasterConfigurationSupport extends ConfigSupport implements MasterConfiguration
{
    public MasterConfigurationSupport(Config... config)
    {
        super(new CompositeConfig(config));
    }

    public boolean isEventLoggingEnabled()
    {
        return getBooleanProperty(LOG_EVENTS, false);
    }

    public void setEventLoggingEnabled(boolean b)
    {
        setBooleanProperty(LOG_EVENTS, b);
    }

    public String getLoggingLevel()
    {
        return getProperty(LOGGING_CONFIG, "default");
    }

    public void setLoggingLevel(String logConfig)
    {
        setProperty(LOGGING_CONFIG, logConfig);
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

    public String getAgentHost()
    {
        String host = getProperty(AGENT_HOST);
        if(host == null)
        {
            // Base it on the base URL
            String base = getBaseUrl();
            if(base != null)
            {
                // Pull out just the host part
                try
                {
                    URL url = new URL(getBaseUrl());
                    host = url.getHost();
                }
                catch (MalformedURLException e)
                {
                    // Nice try
                }
            }

            if(host == null)
            {
                // So much for that plan...let's try and get the host name
                try
                {
                    InetAddress address = InetAddress.getLocalHost();
                    host = address.getCanonicalHostName();
                }
                catch (UnknownHostException e)
                {
                    // Oh well, we tried
                    host = "localhost";
                }
            }
        }

        return host;
    }

    public void setAgentHost(String url)
    {
        setProperty(AGENT_HOST, url);
    }

    public String getHelpUrl()
    {
        return getProperty(HELP_URL, "http://confluence.zutubi.com/display/pulse0102");
    }

    public void setHelpUrl(String helpUrl)
    {
        setProperty(HELP_URL, helpUrl);
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
        return getBooleanProperty(RSS_ENABLED, Boolean.TRUE);
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

    public Boolean getAnonymousSignupEnabled()
    {
        return getBooleanProperty(ANONYMOUS_SIGNUP_ENABLED, Boolean.FALSE);
    }

    public void setAnonymousSignupEnabled(Boolean signupEnabled)
    {
        setBooleanProperty(ANONYMOUS_SIGNUP_ENABLED, signupEnabled);
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

    public Integer getScmPollingInterval()
    {
        return getInteger(SCM_POLLING_INTERVAL, Integer.valueOf(5));
    }

    public void setScmPollingInterval(Integer interval)
    {
        setInteger(SCM_POLLING_INTERVAL, interval);
    }

    public long getUnsatisfiableRecipeTimeout()
    {
        return getLong(UNSATISFIABLE_RECIPE_TIMEOUT, UNSATISFIABLE_RECIPE_TIMEOUT_DEFAULT);
    }

    public void setUnsatisfiableRecipeTimeout(Long timeout)
    {
        setLong(UNSATISFIABLE_RECIPE_TIMEOUT, timeout);
    }

    public boolean isMasterEnabled()
    {
        return getBooleanProperty(MASTER_ENABLED, Boolean.TRUE);
    }

    public void setMasterEnabled(Boolean b)
    {
        setBooleanProperty(MASTER_ENABLED, b);
    }
}
