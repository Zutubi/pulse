package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.servercore.bootstrap.SystemConfiguration;
import com.zutubi.pulse.tove.config.admin.GlobalConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 */
public class DefaultMasterLocationProvider implements MasterLocationProvider
{
    private ConfigurationProvider configurationProvider;
    private SystemConfiguration systemConfiguration;

    public String getMasterLocation()
    {
        String url = configurationProvider.get(GlobalConfiguration.class).getMasterHost() + ":" + systemConfiguration.getServerPort() + systemConfiguration.getContextPath();
        if(url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }

        return url;
    }

    public String getMasterUrl()
    {
        String protocol = "http://";
        if(systemConfiguration.isSslEnabled())
        {
            protocol = "https://";
        }
        
        return protocol + getMasterLocation();
    }

    public void setSystemConfiguration(SystemConfiguration systemConfiguration)
    {
        this.systemConfiguration = systemConfiguration;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
