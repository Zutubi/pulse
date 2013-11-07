package com.zutubi.pulse.master.migrate;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.util.JDBCUtils;
import com.zutubi.pulse.master.bootstrap.Data;
import com.zutubi.pulse.master.bootstrap.SimpleMasterConfigurationManager;
import com.zutubi.pulse.master.database.DatabaseConfig;
import com.zutubi.pulse.master.hibernate.HackyConnectionProvider;
import com.zutubi.pulse.master.hibernate.HibernateUtils;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.master.hibernate.SchemaRefactor;
import com.zutubi.pulse.master.util.monitor.JobManager;
import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.io.IOUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.cfg.Environment;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class MigrationManagerTest extends PulseTestCase
{
    private MigrationManager migrationManager;

    private JobManager jobManager;

    private TestDatabase sourceDatabase;
    private TestDatabase targetDatabase;

    private File tmp;

    private SimpleMasterConfigurationManager configurationManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();

        jobManager = new JobManager();
        
        migrationManager = new MigrationManager();
        migrationManager.setJobManager(jobManager);

        List<String> mappings = new LinkedList<String>();
        mappings.add("com/zutubi/pulse/master/migrate/schema/Schema.hbm.xml");

        sourceDatabase = new TestDatabase();
        sourceDatabase.setMappings(mappings);
        sourceDatabase.init();

        targetDatabase = new TestDatabase();
        targetDatabase.init();

        configurationManager = new SimpleMasterConfigurationManager()
        {
            public File getDataDirectory()
            {
                return new File(tmp, "test");
            }

            public MasterUserPaths getUserPaths()
            {
                return new Data(getDataDirectory());
            }
        };
        File dbConfig = configurationManager.getDatabaseConfigFile();
        assertTrue(dbConfig.getParentFile().mkdirs());
        assertTrue(dbConfig.createNewFile());
        IOUtils.write(sourceDatabase.getJdbcProperties(), dbConfig);
        
        migrationManager.setConfigurationManager(configurationManager);
    }

    protected void tearDown() throws Exception
    {
        System.clearProperty(MigrationManager.REQUEST_MIGRATE_SYSTEM_KEY);

        sourceDatabase.shutdown();
        targetDatabase.shutdown();

        removeDirectory(tmp);

        super.tearDown();
    }

    public void testIsRequested()
    {
        assertFalse(migrationManager.isRequested());

        System.setProperty(MigrationManager.REQUEST_MIGRATE_SYSTEM_KEY, Boolean.toString(true));

        assertTrue(migrationManager.isRequested());
    }

    public void testSampleMigrationFromAToB() throws IOException
    {
        // want to migrate from database (A) to database (B).

        // need configurations for
        // - database A

        // - hibernate configuration for migration
        migrationManager.setHibernateConfiguration(sourceDatabase.getHibernateConfig());

        // should have a running database now, using the above details.

        // - database B
        // database B properties are specified by the user via the UI.
        Properties databaseB = targetDatabase.getJdbcProperties();

        // migration is a long running task
        // - migration manager provides a unified view of the migration process, even
        //   though it is delegated to the job manager.

        migrationManager.scheduleMigration(databaseB);
        migrationManager.runMigration();

        Monitor monitor = migrationManager.getMonitor();
        assertTrue(monitor.isSuccessful());

        DatabaseConfig migratedConfig = configurationManager.getDatabaseConfig();
        assertEquals(databaseB.getProperty(DatabaseConfig.JDBC_URL), migratedConfig.getUrl());
    }

    private static class TestDatabase
    {
        private Properties jdbcProperties;

        private List<String> mappings = new LinkedList<String>();

        private DataSource dataSource;
        private MutableConfiguration hibernateConfig;

        public TestDatabase()
        {
        }

        public void init() throws SQLException, IOException
        {
            String databaseName = RandomUtils.insecureRandomString(6);

            jdbcProperties = new Properties();
            jdbcProperties.setProperty(DatabaseConfig.JDBC_URL, "jdbc:hsqldb:mem:" + databaseName);
            jdbcProperties.setProperty(DatabaseConfig.JDBC_USERNAME, "sa");
            jdbcProperties.setProperty(DatabaseConfig.JDBC_PASSWORD, "");

            // create the database.

            hibernateConfig = new MutableConfiguration();
            hibernateConfig.addClassPathMappings(mappings);

            Properties hibernateProperties = new Properties();
            hibernateProperties.setProperty(DatabaseConfig.HIBERNATE_DIALECT, HibernateUtils.inferHibernateDialect(jdbcProperties));
            hibernateProperties.put(Environment.CONNECTION_PROVIDER, "com.zutubi.pulse.master.hibernate.HackyConnectionProvider");

            HackyConnectionProvider.dataSource = getDataSource();

            SchemaRefactor refactor = new SchemaRefactor(hibernateConfig, hibernateProperties);
            refactor.createSchema();
        }

        public Properties getJdbcProperties()
        {
            return jdbcProperties;
        }

        public MutableConfiguration getHibernateConfig()
        {
            return hibernateConfig;
        }

        public void setMappings(List<String> mappings)
        {
            this.mappings = mappings;
        }

        public DataSource getDataSource()
        {
            if (dataSource == null)
            {
                synchronized(this)
                {
                    if (dataSource == null)
                    {
                        BasicDataSource dataSource = new BasicDataSource();
                        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
                        dataSource.setUrl(getJdbcProperties().getProperty(DatabaseConfig.JDBC_URL));
                        dataSource.setPassword(getJdbcProperties().getProperty(DatabaseConfig.JDBC_PASSWORD));
                        dataSource.setUsername(getJdbcProperties().getProperty(DatabaseConfig.JDBC_USERNAME));

                        this.dataSource = dataSource;
                    }
                }
            }
            return dataSource;
        }

        public void shutdown() throws SQLException
        {
            if (dataSource != null)
            {
                JDBCUtils.execute(dataSource, "SHUTDOWN COMPACT");
            }
        }
    }
}
