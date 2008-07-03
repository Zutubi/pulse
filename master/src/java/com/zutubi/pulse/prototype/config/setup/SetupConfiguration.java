package com.zutubi.pulse.prototype.config.setup;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.migrate.MigrateDatabaseTypeConfiguration;

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
    private SetupLicenseConfiguration license;
    private RequestLicenseConfiguration requestLicense;
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

    public SetupLicenseConfiguration getLicense()
    {
        return license;
    }

    public void setLicense(SetupLicenseConfiguration license)
    {
        this.license = license;
    }

    public RequestLicenseConfiguration getRequestLicense()
    {
        return requestLicense;
    }

    public void setRequestLicense(RequestLicenseConfiguration requestLicense)
    {
        this.requestLicense = requestLicense;
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
