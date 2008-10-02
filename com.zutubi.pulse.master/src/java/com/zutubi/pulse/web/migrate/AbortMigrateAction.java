package com.zutubi.pulse.web.migrate;

import com.zutubi.pulse.master.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.master.bootstrap.SetupManager;

/**
 * This is triggered when the user decides that they do not want to
 * migrate there database after all.
 *
 */
public class AbortMigrateAction extends MigrateActionSupport
{
    private SetupManager setupManager;

    public String execute() throws Exception
    {
        migrationManager.cancelMigration();

        // return to the setup manager and continue with the setup workflow.
        ((DefaultSetupManager)setupManager).doCancelMigrationRequest();
        
        return SUCCESS;
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
