package com.zutubi.pulse.tove.config.user;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.tove.config.DatabaseStateCleanupTaskSupport;
import com.zutubi.tove.config.ToveRuntimeException;

/**
 * Cleans up the state associated with a deleted user.
 */
class UserStateCleanupTask extends DatabaseStateCleanupTaskSupport
{
    private UserConfiguration instance;
    private UserManager userManager;
    private BuildManager buildManager;

    public UserStateCleanupTask(UserConfiguration instance, UserManager userManager, BuildManager buildManager)
    {
        super(instance.getConfigurationPath(), buildManager);
        this.instance = instance;
        this.userManager = userManager;
        this.buildManager = buildManager;
    }

    public void cleanupState()
    {
        User user = userManager.getUser(instance.getUserId());
        if (user != null)
        {
            // Wait for a running personal build to complete
            BuildResult build = buildManager.getLatestBuildResult(user);
            if(build != null && build.inProgress())
            {
                throw new ToveRuntimeException("Unable to delete user: the user has a personal build in progress.");
            }
            userManager.delete(user);
        }
    }
}
