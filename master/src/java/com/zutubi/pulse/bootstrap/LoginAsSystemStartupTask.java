package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.security.AcegiUtils;

/**
 * A startup task which logs the main thread in as the system user, allowing
 * it to call secured methods.
 */
public class LoginAsSystemStartupTask implements StartupTask
{
    public void execute()
    {
        AcegiUtils.loginAsSystem();
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
