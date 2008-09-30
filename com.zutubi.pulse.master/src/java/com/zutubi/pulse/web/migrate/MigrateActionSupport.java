package com.zutubi.pulse.web.migrate;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.migrate.MigrationManager;

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
