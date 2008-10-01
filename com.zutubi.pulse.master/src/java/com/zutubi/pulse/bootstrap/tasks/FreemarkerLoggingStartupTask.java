package com.zutubi.pulse.bootstrap.tasks;

import com.zutubi.pulse.freemarker.CustomFreemarkerManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

/**
 * Sets up Freemarker logging during the startup process.
 */
public class FreemarkerLoggingStartupTask implements StartupTask
{
    public void execute()
    {
        CustomFreemarkerManager.initialiseLogging();
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
