package com.zutubi.pulse.migrate;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.MasterUserPaths;
import com.zutubi.pulse.database.DatabaseConfig;
import com.zutubi.pulse.monitor.JobManager;
import com.zutubi.pulse.monitor.Monitor;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * The migration manager handles the workflow for migrating to a new database.
 *
 */
public class MigrationManager
{
    public static final String MIGRATE_JOB_KEY = "migrate";

    public static final String REQUEST_MIGRATE_SYSTEM_KEY = "migrate.database";

    private JobManager jobManager = null;

    private DatabaseConfig currentDatabaseConfig;

    private MasterUserPaths userPaths;

    private List<String> hibernateMappings = null;

    private MasterConfigurationManager configurationManager;

    public void scheduleMigration(Properties databaseProperties) throws IOException
    {
        MigrateDatabaseTask migrateTask = new MigrateDatabaseTask("Migrate database");
        migrateTask.setTargetJdbcProperties(databaseProperties);
        migrateTask.setSourceJdbcProperties(currentDatabaseConfig.getProperties());
        migrateTask.setUserPaths(userPaths);
        migrateTask.setMappings(hibernateMappings);

        UpdateDatabaseConfigurationFileTask updateTask = new UpdateDatabaseConfigurationFileTask("Update configuration");
        updateTask.setConfigurationManager(configurationManager);
        updateTask.setUpdatedConfiguration(databaseProperties);

        jobManager.register(MIGRATE_JOB_KEY, migrateTask, updateTask);
    }

    public Monitor getMonitor()
    {
        return jobManager.getMonitor(MIGRATE_JOB_KEY);
    }

    public void runMigration()
    {
        jobManager.start(MIGRATE_JOB_KEY);

        // somewhere here we need to update the configured database properties.

    }

    public boolean isRequested()
    {
        return Boolean.getBoolean(REQUEST_MIGRATE_SYSTEM_KEY);
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager) throws IOException
    {
        setDatabaseConfig(configurationManager.getDatabaseConfig());
        setUserPaths(configurationManager.getUserPaths());
        this.configurationManager = configurationManager;
    }

    public void setHibernateMappings(List<String> hibernateMappings)
    {
        this.hibernateMappings = hibernateMappings;
    }

    public void setJobManager(JobManager jobManager)
    {
        this.jobManager = jobManager;
    }

    // place holder for any processing required on the cancellation of the migration request.
    public void cancelMigration()
    {

    }

    public void setDatabaseConfig(DatabaseConfig currentDatabaseConfig)
    {
        this.currentDatabaseConfig = currentDatabaseConfig;
    }

    public void setUserPaths(MasterUserPaths userPaths)
    {
        this.userPaths = userPaths;
    }
}
