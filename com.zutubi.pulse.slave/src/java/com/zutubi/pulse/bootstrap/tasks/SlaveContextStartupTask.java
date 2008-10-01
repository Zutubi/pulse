package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.bootstrap.StartupTask;
import com.zutubi.pulse.core.spring.SpringComponentContext;

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
