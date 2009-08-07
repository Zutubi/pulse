package com.zutubi.pulse.servercore.bootstrap;

import com.zutubi.util.StringUtils;
import com.zutubi.util.config.CompositeConfig;
import com.zutubi.util.config.Config;
import com.zutubi.util.config.ConfigSupport;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class SystemConfigurationSupport extends ConfigSupport implements SystemConfiguration
{
    private String configFile;

    public SystemConfigurationSupport(String configFile, Config... config)
    {
        super(new CompositeConfig(config));
        this.configFile = configFile;
    }

    public String getConfigFilePath()
    {
        return configFile;
    }

    public String getBindAddress()
    {
        String result = delegate.getProperty(WEBAPP_BIND_ADDRESS);
        if(StringUtils.stringSet(result))
        {
            return result;
        }
        else
        {
            return DEFAULT_WEBAPP_BIND_ADDRESS;
        }
    }

    public int getServerPort()
    {
        return getInteger(WEBAPP_PORT, DEFAULT_WEBAPP_PORT);
    }

    public String getContextPath()
    {
        return getProperty(CONTEXT_PATH, DEFAULT_CONTEXT_PATH);
    }

    public String getRestoreFile()
    {
        return delegate.getProperty(RESTORE_FILE);
    }

    public String getRestoreArtifacts()
    {
        return delegate.getProperty(RESTORE_ARTIFACTS);
    }

    /**
     * Ensure that the returned context path has a leading '/' and no trailing '/'.
     * 
     * @return the web app context path in a normalised form suitable for
     *         appending further path elements like: contextPath + "/" + path
     */
    public String getContextPathNormalised()
    {
        String contextPath = getContextPath();
        if(!contextPath.startsWith("/"))
        {
            contextPath = "/" + contextPath;
        }
        if(contextPath.endsWith("/"))
        {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }
        return contextPath;
    }

    public void setDataPath(String path)
    {
        setProperty(PULSE_DATA, path);
    }

    public String getDataPath()
    {
        return getProperty(PULSE_DATA);
    }

    public String getHostUrl()
    {
        String hostname = "localhost";
        try
        {
            InetAddress address = InetAddress.getLocalHost();
            hostname = address.getCanonicalHostName();
        }
        catch (UnknownHostException e)
        {
            // noop.
        }
        String hostUrl = "http://" + hostname + ":" + getServerPort();
        if (!getContextPath().startsWith("/"))
        {
            hostUrl = hostUrl + "/";
        }
        return hostUrl + getContextPath();
    }

    public boolean isSslEnabled()
    {
        return getBooleanProperty(SSL_ENABLED, false);
    }

    public String getSslKeystore()
    {
        return getProperty(SSL_KEYSTORE);
    }

    public String getSslPassword()
    {
        return getProperty(SSL_PASSWORD);
    }

    public String getSslKeyPassword()
    {
        return getProperty(SSL_KEY_PASSWORD);
    }
}
