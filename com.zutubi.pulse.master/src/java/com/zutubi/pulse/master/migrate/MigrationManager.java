/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.migrate;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.master.util.monitor.JobManager;
import com.zutubi.pulse.master.util.monitor.Monitor;

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

    private MutableConfiguration hibernateConfiguration = null;

    private Properties jdbcProperties;

    private MasterConfigurationManager configurationManager;

    private List<String> mappings;

    public void scheduleMigration(Properties databaseProperties) throws IOException
    {
        if (hibernateConfiguration == null)
        {
            hibernateConfiguration = new MutableConfiguration();
            hibernateConfiguration.addClassPathMappings(mappings);
            Properties hibernateProperties = configurationManager.getDatabaseConfig().getHibernateProperties();
            hibernateConfiguration.setProperties(hibernateProperties);
        }

        // since we are (/may be) dealing with dynamically loaded drivers, these jdbc properties
        // should not contain direct jdbc classname references.  The drivers will be looked up via the URLs.

        // make copies so as not to change the originals.
        Properties source = new Properties();
        source.putAll(jdbcProperties);
        source.remove(DatabaseConfig.JDBC_DRIVER_CLASS_NAME);

        Properties target = new Properties();
        target.putAll(databaseProperties);
        target.remove(DatabaseConfig.JDBC_DRIVER_CLASS_NAME);
        
        MigrateDatabaseTask migrateTask = new MigrateDatabaseTask("Migrate database");
        migrateTask.setTargetJdbcProperties(target);
        migrateTask.setSourceJdbcProperties(source);
        migrateTask.setHibernateConfiguration(hibernateConfiguration);

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
        jobManager.run(MIGRATE_JOB_KEY);
    }

    public boolean isRequested()
    {
        return Boolean.getBoolean(REQUEST_MIGRATE_SYSTEM_KEY);
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager) throws IOException
    {
        setDatabaseConfig(configurationManager.getDatabaseConfig());
        this.configurationManager = configurationManager;
    }

    public void setJobManager(JobManager jobManager)
    {
        this.jobManager = jobManager;
    }

    // place holder for any processing required on the cancellation of the migration request.
    public void cancelMigration()
    {

    }

    public void setDatabaseProperties(Properties props)
    {
        this.jdbcProperties = props;
    }

    public void setDatabaseConfig(DatabaseConfig currentDatabaseConfig)
    {
        Properties props = new Properties();
        props.putAll(currentDatabaseConfig.getProperties());
        // ensure that the url is resolved if needed.
        props.setProperty(DatabaseConfig.JDBC_URL, currentDatabaseConfig.getUrl());

        setDatabaseProperties(props);
    }

    public void setHibernateConfiguration(MutableConfiguration hibernateConfiguration)
    {
        this.hibernateConfiguration = hibernateConfiguration;
    }

    public void setHibernateMappings(List<String> mappings)
    {
        this.mappings = mappings;
    }
}
