package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.servercore.api.AdminTokenManager;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

/**
 * A task that creates the admin token used by pulse command line tools to
 * authenticate with the remote API.  This task is run after we bind the webapp
 * socket - when we're pretty confident we're not going to collide with another
 * running Pulse instance.
 */
public class AdminTokenStartupTask implements StartupTask
{
    private AdminTokenManager adminTokenManager;

    public void execute()
    {
        adminTokenManager.init();
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setAdminTokenManager(AdminTokenManager adminTokenManager)
    {
        this.adminTokenManager = adminTokenManager;
    }
}