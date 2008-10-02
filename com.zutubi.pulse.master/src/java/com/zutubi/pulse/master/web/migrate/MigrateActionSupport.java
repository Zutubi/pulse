package com.zutubi.pulse.master.web.migrate;

import com.zutubi.pulse.master.migrate.MigrationManager;
import com.zutubi.pulse.master.web.ActionSupport;

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
