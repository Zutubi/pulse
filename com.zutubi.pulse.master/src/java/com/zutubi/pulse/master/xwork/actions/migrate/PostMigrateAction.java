package com.zutubi.pulse.master.xwork.actions.migrate;

import com.zutubi.pulse.master.bootstrap.SetupManager;
import com.zutubi.pulse.master.migrate.MigrationManager;
import com.zutubi.pulse.master.util.monitor.Monitor;

/**
 *
 *
 */
public class PostMigrateAction extends MigrateActionSupport
{
    private Monitor monitor;

    private SetupManager setupManager;

    public Monitor getMonitor()
    {
        return monitor;
    }

    public void setMigrationManager(MigrationManager migrationManager)
    {
        this.monitor = migrationManager.getMonitor();
        super.setMigrationManager(migrationManager);
    }


    public String execute() throws Exception
    {
        setupManager.migrateComplete();
        
        return super.execute();
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
