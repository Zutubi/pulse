package com.zutubi.pulse.master.xwork.actions.migrate;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.database.DriverRegistry;
import com.zutubi.pulse.master.migrate.MigrateDatabaseTypeConfiguration;
import com.zutubi.pulse.master.migrate.MigrationManager;
import com.zutubi.pulse.master.tove.config.setup.DatabaseType;
import com.zutubi.pulse.master.tove.webwork.TransientAction;
import com.zutubi.pulse.master.webwork.SessionTokenManager;

import java.io.File;
import java.util.Properties;

/**
 *
 *
 */

//TODO: We should be able to simply render a form given a class name, then create / validate that class
//TODO: again given that classname only.  That layer should be independent of the persistence mechanism,
//TODO: and will make these random pages much easier to understand and add.

public class PreviewMigrateAction extends TransientAction<MigrateDatabaseTypeConfiguration>
{
    private DatabaseConfig databaseConfig;

    private MasterConfigurationManager configurationManager;

    private MigrationManager migrationManager;

    private MigrateDatabaseTypeConfiguration configuration;

    public PreviewMigrateAction()
    {
        super("init/migrateDatabaseType");
    }

    protected MigrateDatabaseTypeConfiguration initialise() throws Exception
    {
        databaseConfig = configurationManager.getDatabaseConfig();

        String url = databaseConfig.getUrl();
        if (url.startsWith("jdbc:mysql"))
        {
            configuration = DatabaseType.MYSQL.getDatabaseConfiguration(databaseConfig.getProperties());
        }
        else if (url.startsWith("jdbc:postgresql"))
        {
            configuration = DatabaseType.POSTGRESQL.getDatabaseConfiguration(databaseConfig.getProperties());
        }
        else if (url.startsWith("jdbc:hsqldb"))
        {
            configuration = DatabaseType.EMBEDDED.getDatabaseConfiguration(databaseConfig.getProperties());
        }

        return new MigrateDatabaseTypeConfiguration();
    }

    public DatabaseConfig getDatabaseConfig()
    {
        return databaseConfig;
    }

    public MigrateDatabaseTypeConfiguration getOriginalConfiguration()
    {
        return configuration;
    }

    protected String complete(MigrateDatabaseTypeConfiguration instance) throws Exception
    {
        SessionTokenManager.validateSessionToken();
        
        if (!instance.getType().isEmbedded())
        {
            if (TextUtils.stringSet(instance.getDriverFile()))
            {
                // install the driver... this should be handled elsewhere...
                DriverRegistry driverRegistry = configurationManager.getDriverRegistry();

                File driverFile = new File(instance.getDriverFile());
                driverRegistry.register(instance.getDriver(), driverFile);
            }
        }

        Properties props = instance.getDatabaseProperties();

        migrationManager.scheduleMigration(props);

        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setMigrationManager(MigrationManager migrationManager)
    {
        this.migrationManager = migrationManager;
    }
}

