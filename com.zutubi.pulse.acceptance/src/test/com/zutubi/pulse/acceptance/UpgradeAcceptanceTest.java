package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.pulse.master.hibernate.MutableConfiguration;
import com.zutubi.pulse.master.license.LicenseException;
import com.zutubi.pulse.master.license.LicenseHelper;
import com.zutubi.pulse.master.license.LicenseType;
import com.zutubi.pulse.master.transfer.TransferAPI;
import com.zutubi.pulse.master.transfer.TransferException;
import com.zutubi.pulse.servercore.cli.ShutdownCommand;
import com.zutubi.pulse.servercore.cli.StartCommand;
import static com.zutubi.util.Constants.SECOND;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import org.apache.commons.cli.ParseException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class UpgradeAcceptanceTest extends SeleniumTestBase
{
    File dataArea = new File(TestUtils.getPulseRoot(), FileSystemUtils.composeFilename("com.zutubi.pulse.acceptance", "src", "test", "data"));
    private File tmpDir = null;
    private DriverManagerDataSource dataSource = new DriverManagerDataSource();
    private String dialect = "org.hibernate.dialect.HSQLDialect";
    private String db;

    protected void setUp() throws Exception
    {
        AcceptanceTestUtils.setPulsePort(8990);

        super.setUp();

        tmpDir = FileSystemUtils.createTempDir("UAT", "");
        db = System.getenv("PULSE_DB");
    }

    protected void tearDown() throws Exception
    {
        if (!FileSystemUtils.rmdir(tmpDir))
        {
            //throw new RuntimeException("Failed to remove the temporary directory: " + tmpDir.getAbsolutePath());
            System.out.println("failed to remove tmp directory due to earlier error.");
        }
        super.tearDown();
    }

    public void testPostBuildActionSpecifications() throws Exception
    {
        importAndUpgradeTest("0102015000");
    }

    public void testDanglingSlaveHostRequirements() throws Exception
    {
        importAndUpgradeTest("0102017001");
    }

    public void testFeatureStorage() throws Exception
    {
        importAndUpgradeTest("0102018000");
    }

    public void testDuplicateChangelists() throws Exception
    {
        importAndUpgradeTest("0102018001");
    }

    public void testSubscriptionConditions() throws Exception
    {
        importAndUpgradeTest("0102019000");
    }

    public void testLongRevisionComments() throws Exception
    {
        // the import will not run for postgres, but this is ok as a postgres
        // dm cannot contain a long revision comment anyway
        if (!"postgres".equals(db))
        {
            importAndUpgradeTest("0102030000");
        }
    }

    public void importAndUpgradeTest(String build) throws Exception
    {
        if ("mysql".equals(db))
        {
            setupMySQL();
        }
        else if ("postgresql".equals(db))
        {
            setupPostgreSQL();
        }
        else
        {
            setupHSQL();
        }

        File buildDir = new File(dataArea, build);

        File configFile = new File(buildDir, "pulse.properties");
        File configDir = new File(tmpDir, "config");
        if (!configDir.exists() && !configDir.mkdirs())
        {
            // is this a fatal issue?.
        }
        FileSystemUtils.copy(configDir, configFile);

        restoreDatabase(buildDir);

        setupServerProperties(build);
        System.setProperty("bootstrap", "com/zutubi/pulse/master/bootstrap/ideaBootstrapContext.xml");

        runUpgrade(build);
    }

    private void restoreDatabase(File buildDir) throws IOException, TransferException
    {
        MutableConfiguration configuration = new MutableConfiguration();
        File mappingsDir = new File(buildDir, "mappings");
        File[] mappings = mappingsDir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".hbm.xml");
            }
        });

        configuration.addFileSystemMappings(Arrays.asList(mappings));
        configuration.setProperty("hibernate.dialect", dialect);

        TransferAPI transferAPI = new TransferAPI();
        transferAPI.restore(configuration, new File(buildDir, "dump.xml"), dataSource);
    }

    private void setupMySQL()
    {
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/mysql");
        dataSource.setUsername("pulsetest");
        dataSource.setPassword("pulsetest");

        JdbcTemplate template = new JdbcTemplate(dataSource);
        template.update("drop schema if exists pulse_accept");
        template.update("create schema pulse_accept");

        dataSource.setUrl("jdbc:mysql://localhost:3306/pulse_accept");
        dialect = "org.hibernate.dialect.MySQLDialect";
    }

    private void setupPostgreSQL()
    {
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/template1");
        dataSource.setUsername("pulsetest");
        dataSource.setPassword("pulsetest");

        JdbcTemplate template = new JdbcTemplate(dataSource);
        Long count = template.queryForLong("select count(*) from pg_database where datname = 'pulse_accept'");
        if (count > 0)
        {
            template.update("drop database pulse_accept");
        }

        template.update("create database pulse_accept");

        dataSource.setUrl("jdbc:postgresql://localhost:5432/pulse_accept");
        dialect = "org.hibernate.dialect.PostgreSQLDialect";
    }

    private void setupHSQL() throws SQLException
    {
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:" + new File(tmpDir, "data").getAbsolutePath() + "/db");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dialect = "org.hibernate.dialect.HSQLDialect";
    }

    private void setupServerProperties(String build) throws IOException, LicenseException
    {
        File propFile = new File(tmpDir, "pulse.config.properties");
        Properties props = new Properties();
        props.put("build.date", "@BUILD_DATE@");
        props.put("build.number", build);
        props.put("release.date", "@RELEASE_DATE@");
        props.put("version.number", "@VERSION@");
        props.put("license.key", LicenseHelper.newLicenseKey(LicenseType.EVALUATION, "S. O. MeBody"));
        dumpProperties(propFile, props);

        File configDir = new File(tmpDir, "config");
        if (!configDir.exists() && !configDir.mkdirs())
        {
            // is this a fatal issue?.
        }
        propFile = new File(configDir, "database.properties");
        props = new Properties();
        props.put("jdbc.driverClassName", dataSource.getDriverClassName());
        props.put("jdbc.url", dataSource.getUrl());
        props.put("jdbc.username", dataSource.getUsername());
        props.put("jdbc.password", dataSource.getPassword());
        props.put("hibernate.dialect", dialect);
        dumpProperties(propFile, props);
    }

    private void dumpProperties(File propFile, Properties props) throws IOException
    {
        FileOutputStream out = null;

        try
        {
            out = new FileOutputStream(propFile);
            props.store(out, null);
        }
        finally
        {
            IOUtils.close(out);
        }
    }

    public void testUpgradeFromVersionOnePointOne() throws Exception
    {
        System.setProperty("bootstrap", "com/zutubi/pulse/master/bootstrap/ideaBootstrapContext.xml");

        // extract zip file.
        File data = new File(dataArea, "pulse-1.1.0-data.zip");
        PulseZipUtils.extractZip(data, tmpDir);

        runUpgrade("0101000000");
    }

    private void runUpgrade(String build) throws Exception
    {
        // start pulse using the extracted data directory.
        final StartCommand start = new StartCommand();

        Thread serverStartup = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    assertEquals(0, start.execute("-p", "8990", "-d", tmpDir.getAbsolutePath()));
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }
            }
        });
        serverStartup.start();

        // now we need to go to the Web UI and wait.

        Thread.sleep(30000);

        browser.goTo("/");

        browser.waitForElement("upgrade.preview", 120 * SECOND);

        // check that we have received the upgrade preview, and that the data is as expected.
        assertTextPresent("Upgrade Preview");
        assertTextPresent(build);

        browser.click("continue");

        browser.waitForElement("upgrade.progress", 120 * SECOND);

        // waiting..
        assertTextPresent("Upgrade Progress");

        browser.waitForElement("upgrade.complete", 120 * SECOND);

        assertTextPresent("Upgrade Complete");
        assertTextPresent("The upgrade has been successful");

        browser.click("continue");

        Thread.sleep(30000);

        ShutdownCommand shutdown = new ShutdownCommand();
        shutdown.setExitJvm(false);
        assertEquals(0, shutdown.execute("-F", "true", "-p", "8990"));
        waitForServerToExit(8990);
        
        // allow time for the shutdown to complete.
        Thread.sleep(3000);
    }

    protected void waitForServerToExit(int port) throws IOException
    {
        int retries = 0;

        while(retries++ < 30)
        {
            Socket sock = new Socket();
            try
            {
                sock.connect(new InetSocketAddress(port));
                sock.close();
                try
                {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    // Empty
                }
            }
            catch (IOException e)
            {
                break;
            }
        }
    }
}
