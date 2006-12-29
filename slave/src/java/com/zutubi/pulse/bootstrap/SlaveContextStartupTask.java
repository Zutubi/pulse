package com.zutubi.pulse.bootstrap;

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
