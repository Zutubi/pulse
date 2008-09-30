package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.bootstrap.StartupTask;
import com.zutubi.pulse.spring.SpringComponentContext;

/**
 *
 *
 */
public class CoreSystemStartupTask implements StartupTask
{
    public void execute()
    {
        SpringComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/bootstrap/context/coreSystemContext.xml");
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
