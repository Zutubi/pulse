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
 */
public class MasterConfigurationSupport extends ConfigSupport implements MasterConfiguration
{
    public MasterConfigurationSupport(Config... config)
    {
        super(new CompositeConfig(config));
    }

    public String getAdminLogin()
    {
        return getProperty(ADMIN_LOGIN);
    }

    public void setAdminLogin(String login)
    {
        setProperty(ADMIN_LOGIN, login);
    }

    public String getAgentHost()
    {
        String host = getProperty(AGENT_HOST);
        if(host == null)
        {
            // FIXME
            // Base it on the base URL
//            String base = getBaseUrl();
//            if(base != null)
//            {
//                // Pull out just the host part
//                try
//                {
//                    URL url = new URL(getBaseUrl());
//                    host = url.getHost();
//                }
//                catch (MalformedURLException e)
//                {
//                    // Nice try
//                }
//            }

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

    public boolean isMasterEnabled()
    {
        return getBooleanProperty(MASTER_ENABLED, Boolean.TRUE);
    }

    public void setMasterEnabled(Boolean b)
    {
        setBooleanProperty(MASTER_ENABLED, b);
    }
}
