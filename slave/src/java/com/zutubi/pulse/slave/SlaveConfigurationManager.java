package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.AbstractConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemPaths;
import com.zutubi.pulse.bootstrap.SystemConfiguration;

/**
 */
public class SlaveConfigurationManager extends AbstractConfigurationManager
{
    private SystemConfiguration appConfig;
    private SlaveUserPaths userPaths;

    public SlaveConfigurationManager()
    {
    }

    public void init()
    {
        SystemPaths systemPaths = getSystemPaths();
        userPaths = new SlaveUserPaths(systemPaths);
        appConfig = new DefaultSlaveConfiguration(userPaths, systemPaths);
    }

    public SystemConfiguration getSystemConfig()
    {
        return appConfig;
    }

    public SlaveUserPaths getUserPaths()
    {
        return userPaths;
    }
}
