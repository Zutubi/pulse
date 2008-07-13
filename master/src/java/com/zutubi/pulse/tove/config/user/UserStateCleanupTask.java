package com.zutubi.pulse.tove.config.user;

import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.tove.config.cleanup.RecordCleanupTaskSupport;

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
