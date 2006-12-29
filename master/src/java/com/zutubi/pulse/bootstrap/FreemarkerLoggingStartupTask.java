package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.freemarker.CustomFreemarkerManager;

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
