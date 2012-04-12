package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.embedded.EmbeddedPulse;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.pulse.master.bootstrap.Data;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.bootstrap.SimpleMasterConfigurationManager;
import static com.zutubi.pulse.master.database.DatabaseConfig.*;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.master.migrate.MigrationManager;
import com.zutubi.pulse.master.util.monitor.JobManager;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;
import com.zutubi.util.Condition;
import static com.zutubi.util.Constants.SECOND;
import com.zutubi.util.FileSystemUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.openqa.selenium.By;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class UpgradeAcceptanceTest extends AcceptanceTestBase
{
    private File dataArea;
    private File work;
    private int port = 8990;

    @Override
    protected void setUp() throws Exception
    {
        AcceptanceTestUtils.setPulsePort(port);

        dataArea = new File(TestUtils.getPulseRoot(), FileSystemUtils.composeFilename("com.zutubi.pulse.acceptance", "src", "test", "data"));

        super.setUp();

        work = FileSystemUtils.createTempDir("uat", "");

        // ensure drivers are registered before attempting to use them.
        DriverManager.registerDriver((Driver) Class.forName("org.hsqldb.jdbcDriver").newInstance());
        DriverManager.registerDriver((Driver) Class.forName("org.postgresql.Driver").newInstance());
//        DriverManager.registerDriver((Driver) Class.forName("com.mysql.jdbc.Driver").newInstance());
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeDirectory(work);

        super.tearDown();
    }

    public void testMigrateToPostgres() throws Exception
    {
        String version = "2.1.30";

        Properties targetDatabase = postgresJdbcProperties();
        prepareTargetDatabase(targetDatabase);

        File dataDir = dataDir(version);
        List<File> mappings = hibernateMappings(version);

        migrateToTargetDatabase(dataDir, mappings, targetDatabase);
    }

    public void testMigrateToPostgresAndUpgradeToCurrent() throws Exception
    {
        String version = "2.1.30";

        Properties targetDatabase = postgresJdbcProperties();
        prepareTargetDatabase(targetDatabase);

        File dataDir = dataDir(version);
        List<File> mappings = hibernateMappings(version);

        migrateToTargetDatabase(dataDir, mappings, targetDatabase);

        upgradeToCurrent(dataDir);
    }

    public void testUpgrade2050ToCurrentVersion() throws Exception
    {
        File dataDir = dataDir("2.0.50");
        upgradeToCurrent(dataDir);
    }

    public void testUpgrade2130ToCurrentVersion() throws Exception
    {
        File dataDir = dataDir("2.1.30");
        upgradeToCurrent(dataDir);
    }

    private void migrateToTargetDatabase(final File dataDir, final List<File> hibernateMappings, Properties targetDatabase) throws Exception
    {
        MasterConfigurationManager configurationManager = new SimpleMasterConfigurationManager()
        {
            public File getDataDirectory()
            {
                return dataDir;
            }

            public MasterUserPaths getUserPaths()
            {
                return new Data(getDataDirectory());
            }
        };

        MigrationManager migrationManager = new MigrationManager();
        migrationManager.setJobManager(new JobManager());
        migrationManager.setConfigurationManager(configurationManager);

        MutableConfiguration hibernateConfiguration = new MutableConfiguration();
        hibernateConfiguration.addFileSystemMappings(hibernateMappings);
        hibernateConfiguration.setProperties(configurationManager.getDatabaseConfig().getHibernateProperties());
        migrationManager.setHibernateConfiguration(hibernateConfiguration);

        migrationManager.scheduleMigration(targetDatabase);
        migrationManager.runMigration();
    }

    private void upgradeToCurrent(File dataDir) throws Exception
    {
        Pulse pulse = new EmbeddedPulse();
        pulse.setDataDir(dataDir.getCanonicalPath());
        pulse.setPort(port);
        pulse.start(true);

        getBrowser().open(urls.base());
        getBrowser().waitForElement("upgrade.preview", 120 * SECOND);

        // check that we have received the upgrade preview, and that the data is as expected.
        assertTrue(getBrowser().isTextPresent("Upgrade Preview"));

        getBrowser().click(By.id("continue"));
        getBrowser().waitForElement("upgrade.progress", 120 * SECOND);

        // waiting..
        assertTrue(getBrowser().isTextPresent("Upgrade Progress"));

        // how long should we be waiting for the upgrade to complete?
        getBrowser().waitForElement("upgrade.complete", 120 * SECOND);

        assertTrue(getBrowser().isTextPresent("Upgrade Complete"));
        assertTrue(getBrowser().isTextPresent("The upgrade has been successful"));

        getBrowser().click(By.id("continue"));

        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                try
                {
                    rpcClient.RemoteApi.ping();
                    return true;
                }
                catch (Exception e)
                {
                    return false;
                }
            }
        }, 30 * SECOND, "pulse to start up");

        // Until we have a way of detecting that spring has finished setting up,
        // we need to wait or risk shutdown failing.
        Thread.sleep(30 * SECOND);
        
        pulse.stop(30 * SECOND);
    }

    private File dataDir(String version) throws Exception
    {
        PulseZipUtils.extractZip(new File(dataArea, "pulse-"+version+"-data.zip"), new File(work, version));
        return new File(work, version + "/data");
    }

    private List<File> hibernateMappings(String version) throws IOException
    {
        File mappingsZip = new File(dataArea, "pulse-"+version+"-mappings.zip");
        PulseZipUtils.extractZip(mappingsZip, work);
        File[] mappings = work.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".hbm.xml");
            }
        });
        return Arrays.asList(mappings);
    }

    private Properties postgresJdbcProperties()
    {
        Properties postgres = new Properties();
        postgres.setProperty(JDBC_URL, "jdbc:postgresql://localhost:5432/pulse_accept");
        postgres.setProperty(JDBC_USERNAME, "pulse");
        postgres.setProperty(JDBC_PASSWORD, "pulse");
        postgres.setProperty(HIBERNATE_DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        return postgres;
    }

    private Properties mysqlJdbcProperties()
    {
        Properties mysql = new Properties();
        mysql.setProperty(JDBC_URL, "jdbc:mysql://localhost:3306/pulse_accept");
        mysql.setProperty(JDBC_USERNAME, "pulse");
        mysql.setProperty(JDBC_PASSWORD, "pulse");
        mysql.setProperty(HIBERNATE_DIALECT, "org.hibernate.dialect.MySQLDialect");
        return mysql;
    }

    private void prepareTargetDatabase(Properties properties)
    {
        String jdbcUrl = (String) properties.get(JDBC_URL);
        if (jdbcUrl.contains(":postgresql:"))
        {
            preparePostgresDatabase(properties);
        }
        else if (jdbcUrl.contains(":mysql:"))
        {
            prepareMysqlDatabase(properties);
        }
    }

    private void prepareMysqlDatabase(Properties properties)
    {
        String jdbcUrl = (String) properties.get(JDBC_URL);
        String databaseName = jdbcUrl.substring(jdbcUrl.lastIndexOf('/') + 1);

        // we can not connect to the database we are going to drop
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(jdbcUrl.substring(0, jdbcUrl.lastIndexOf('/')) + "/mysql");
        dataSource.setUsername((String) properties.get(JDBC_USERNAME));
        dataSource.setPassword((String) properties.get(JDBC_PASSWORD));

        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.update("drop schema if exists " + databaseName);
        template.update("create schema " + databaseName);
    }

    private void preparePostgresDatabase(Properties properties)
    {
        String jdbcUrl = (String) properties.get(JDBC_URL);
        String databaseName = jdbcUrl.substring(jdbcUrl.lastIndexOf('/') + 1);

        // we can not connect to the database we are going to drop
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(jdbcUrl.substring(0, jdbcUrl.lastIndexOf('/')) + "/template1");
        dataSource.setUsername((String) properties.get(JDBC_USERNAME));
        dataSource.setPassword((String) properties.get(JDBC_PASSWORD));

        JdbcTemplate template = new JdbcTemplate(dataSource);
        Long count = template.queryForLong("select count(*) from pg_database where datname = '"+databaseName+"'");
        if (count > 0)
        {
            template.update("drop database " + databaseName);
        }
        template.update("create database " + databaseName);
    }
}
