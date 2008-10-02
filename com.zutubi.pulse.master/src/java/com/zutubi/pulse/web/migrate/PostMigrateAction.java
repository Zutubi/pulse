package com.zutubi.pulse.web.migrate;

import com.zutubi.pulse.master.bootstrap.SetupManager;
import com.zutubi.pulse.master.migrate.MigrationManager;
import com.zutubi.pulse.monitor.Monitor;

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
        setupManager.requestDbComplete();
        
        return super.execute();
    }

    public void setSetupManager(SetupManager setupManager)
    {
        this.setupManager = setupManager;
    }
}
