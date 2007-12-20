package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.bootstrap.StartupTask;
import com.zutubi.pulse.bootstrap.ComponentContext;

/**
 */
public class SlaveContextStartupTask implements StartupTask
{
    public void execute()
    {
        ComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/bootstrap/applicationContext.xml");
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
