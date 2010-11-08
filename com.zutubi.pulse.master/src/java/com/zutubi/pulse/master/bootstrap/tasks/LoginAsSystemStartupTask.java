package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

/**
 * A startup task which logs the main thread in as the system user, allowing
 * it to call secured methods.
 */
public class LoginAsSystemStartupTask implements StartupTask
{
    public void execute()
    {
        SecurityUtils.loginAsSystem();
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
