package com.zutubi.pulse.cleanup.config;

import com.zutubi.prototype.ConfigurationFormatter;

/**
 *
 *
 */
public class CleanupConfigurationFormatter implements ConfigurationFormatter
{
    public String getWhen(CleanupConfiguration config)
    {
        if(config.getUnit() == CleanupUnit.BUILDS)
        {
            return config.getRetain() + " builds";
        }
        else
        {
            return  config.getRetain() + " days";
        }
    }
}
