package com.zutubi.pulse.master.tove.config.setup;

import com.zutubi.pulse.master.migrate.MigrateDatabaseTypeConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 *
 *
 */
@SymbolicName("zutubi.setupConfig")
public class SetupConfiguration extends AbstractConfiguration
{
    private SetupDataConfiguration data;
    private SetupDatabaseTypeConfiguration databaseType;
    private MigrateDatabaseTypeConfiguration migrateDatabaseType;
    private AdminUserConfiguration admin;
    private ServerSettingsConfiguration server;

    public SetupDataConfiguration getData()
    {
        return data;
    }

    public void setData(SetupDataConfiguration data)
    {
        this.data = data;
    }

    public SetupDatabaseTypeConfiguration getDatabaseType()
    {
        return databaseType;
    }

    public void setDatabaseType(SetupDatabaseTypeConfiguration databaseType)
    {
        this.databaseType = databaseType;
    }

    public MigrateDatabaseTypeConfiguration getMigrateDatabaseType()
    {
        return migrateDatabaseType;
    }

    public void setMigrateDatabaseType(MigrateDatabaseTypeConfiguration migrateDatabaseType)
    {
        this.migrateDatabaseType = migrateDatabaseType;
    }

    public AdminUserConfiguration getAdmin()
    {
        return admin;
    }

    public void setAdmin(AdminUserConfiguration admin)
    {
        this.admin = admin;
    }

    public ServerSettingsConfiguration getServer()
    {
        return server;
    }

    public void setServer(ServerSettingsConfiguration server)
    {
        this.server = server;
    }
}
