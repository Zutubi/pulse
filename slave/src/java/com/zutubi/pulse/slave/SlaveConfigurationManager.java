package com.zutubi.pulse.slave;

import com.zutubi.pulse.bootstrap.AbstractConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemPaths;

/**
 */
public class SlaveConfigurationManager extends AbstractConfigurationManager
{
    private SlaveApplicationConfiguration appConfig;
    private SlaveUserPaths userPaths;

    public SlaveConfigurationManager()
    {
    }

    public void init()
    {
        SystemPaths systemPaths = getSystemPaths();
        userPaths = new SlaveUserPaths(systemPaths);
        appConfig = new DefaultSlaveApplicationConfiguration(userPaths, systemPaths);
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
