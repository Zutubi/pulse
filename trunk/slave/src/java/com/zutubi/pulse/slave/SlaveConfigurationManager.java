package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.AbstractConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.bootstrap.SystemPaths;

/**
 */
public class SlaveConfigurationManager extends AbstractConfigurationManager
{
    private DefaultSlaveConfiguration appConfig;
    private SlaveUserPaths userPaths;

    public SlaveConfigurationManager()
    {
    }

    public void init()
    {
        SystemPaths systemPaths = getSystemPaths();
        appConfig = new DefaultSlaveConfiguration(systemPaths, getEnvConfig());
        userPaths = new SlaveUserPaths(appConfig);
    }

    public SystemConfiguration getSystemConfig()
    {
        return appConfig;
    }

    public SlaveConfiguration getAppConfig()
    {
        return appConfig;
    }

    public SlaveUserPaths getUserPaths()
    {
        return userPaths;
    }
}
