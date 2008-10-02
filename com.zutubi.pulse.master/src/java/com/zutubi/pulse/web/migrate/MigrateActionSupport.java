package com.zutubi.pulse.web.migrate;

import com.zutubi.pulse.master.migrate.MigrationManager;
import com.zutubi.pulse.web.ActionSupport;

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
