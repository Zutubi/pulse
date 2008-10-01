package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

/**
 */
public class SlaveContextStartupTask implements StartupTask
{
    public void execute()
    {
        SpringComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/bootstrap/applicationContext.xml");
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
