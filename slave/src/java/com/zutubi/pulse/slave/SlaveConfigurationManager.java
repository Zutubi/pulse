package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.AbstractCoreConfigurationManager;

/**
 */
public class SlaveConfigurationManager extends AbstractCoreConfigurationManager
{
    private SlaveApplicationConfiguration appConfig;
    private SlaveUserPaths userPaths;

    public SlaveConfigurationManager()
    {
        userPaths = new SlaveUserPaths();
        appConfig = new DefaultSlaveApplicationConfiguration(userPaths);
    }

    public SlaveApplicationConfiguration getAppConfig()
    {
        return appConfig;
    }

    public SlaveUserPaths getUserPaths()
    {
        return userPaths;
    }
}
