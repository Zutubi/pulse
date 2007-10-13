package com.zutubi.pulse.prototype.config.user;

import com.zutubi.prototype.config.cleanup.RecordCleanupTaskSupport;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.core.PulseRuntimeException;

/**
 * Cleans up the state associated with a deleted user.
 */
class UserStateCleanupTask extends RecordCleanupTaskSupport
{
    private UserConfiguration instance;
    private UserManager userManager;
    private BuildManager buildManager;

    public UserStateCleanupTask(UserConfiguration instance, UserManager userManager, BuildManager buildManager)
    {
        super(instance.getConfigurationPath());
        this.instance = instance;
        this.userManager = userManager;
        this.buildManager = buildManager;
    }

    public void run()
    {
        User user = userManager.getUser(instance.getUserId());
        if (user != null)
        {
            // Wait for a running personal build to complete
            BuildResult build = buildManager.getLatestBuildResult(user);
            while(build != null && build.inProgress())
            {
                try
                {
                    Thread.sleep(60000);
                }
                catch (InterruptedException e)
                {
                    // Empty
                }

                build = buildManager.getLatestBuildResult(user);
            }
            userManager.delete(user);
        }
    }

    public boolean isAsynchronous()
    {
        return true;
    }
}
