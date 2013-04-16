package com.zutubi.pulse.master.cleanup.config;

public class AbstractCleanupConfigurationFormatter
{
    public String getSummary(AbstractCleanupConfiguration config)
    {
        return config.summarise();
    }
}
