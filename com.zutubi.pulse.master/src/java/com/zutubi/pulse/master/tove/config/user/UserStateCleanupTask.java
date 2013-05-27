package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.tove.config.DatabaseStateCleanupTaskSupport;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.tove.config.ToveRuntimeException;

/**
 * Cleans up the state associated with a deleted user.
 */
class UserStateCleanupTask extends DatabaseStateCleanupTaskSupport
{
    private UserConfiguration instance;
    private UserManager userManager;
    private BuildManager buildManager;

    public UserStateCleanupTask(UserConfiguration instance, UserManager userManager, BuildManager buildManager, TransactionContext transactionContext)
    {
        super(instance.getConfigurationPath(), transactionContext);
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
            if(build != null && build.running())
            {
                throw new ToveRuntimeException("Unable to delete user: the user has a personal build in progress.");
            }

            // clean up any user responsibility
            userManager.clearAllResponsibilities(user);
            userManager.delete(user);
        }
    }
}
