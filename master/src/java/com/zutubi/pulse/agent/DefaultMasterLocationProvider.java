package com.zutubi.pulse.agent;

import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.SystemConfiguration;

/**
 */
public class DefaultMasterLocationProvider implements MasterLocationProvider
{
    private MasterConfiguration masterConfiguration;
    private SystemConfiguration systemConfiguration;

    public String getMasterLocation()
    {
        String url = masterConfiguration.getAgentHost() + ":" + systemConfiguration.getServerPort() + systemConfiguration.getContextPath();
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

    public void setMasterConfiguration(MasterConfiguration masterConfiguration)
    {
        this.masterConfiguration = masterConfiguration;
    }

    public void setSystemConfiguration(SystemConfiguration systemConfiguration)
    {
        this.systemConfiguration = systemConfiguration;
    }
}
