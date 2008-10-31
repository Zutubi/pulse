package com.zutubi.pulse.master.cleanup.config;

/**
 *
 *
 */
public class CleanupConfigurationFormatter
{
    public String getAfter(CleanupConfiguration config)
    {
        if(config.getRetain() == Integer.MIN_VALUE)
        {
            return "";
        }
        
        if (config.getRetain() == 0)
        {
            return "never";
        }
        
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
