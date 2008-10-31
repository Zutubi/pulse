package com.zutubi.pulse.master.xwork.actions.migrate;

import com.zutubi.pulse.master.migrate.MigrationManager;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 *
 *
 */
public class MigrateActionSupport extends ActionSupport
{
    protected MigrationManager migrationManager;

    public void setMigrationManager(MigrationManager migrationManager)
    {
        this.migrationManager = migrationManager;
    }
}
