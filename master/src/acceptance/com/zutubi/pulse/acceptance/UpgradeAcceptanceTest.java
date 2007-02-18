package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.ShutdownCommand;
import com.zutubi.pulse.command.StartCommand;
import com.zutubi.pulse.license.LicenseException;
import com.zutubi.pulse.license.LicenseType;
import com.zutubi.pulse.test.LicenseHelper;
import com.zutubi.pulse.test.TestUtils;
import com.zutubi.pulse.transfer.TransferAPI;
import com.zutubi.pulse.upgrade.tasks.MutableConfiguration;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.*;
import java.sql.SQLException;
import java.util.Properties;
import java.util.zip.ZipInputStream;

/**
 * <class-comment/>
 */
public class UpgradeAcceptanceTest extends BaseAcceptanceTestCase
{
    File dataArea = new File(TestUtils.getPulseRoot(), FileSystemUtils.composeFilename("master", "src", "acceptance", "data"));
    private File tmpDir = null;
    private DriverManagerDataSource dataSource = new DriverManagerDataSource();
    private String dialect = "org.hibernate.dialect.HSQLDialect";

    protected void setUp() throws Exception
    {
        tmpDir = FileSystemUtils.createTempDir("UAT", "");
    }

    protected void tearDown() throws Exception
    {
        if (!FileSystemUtils.rmdir(tmpDir))
        {
            //throw new RuntimeException("Failed to remove the temporary directory: " + tmpDir.getAbsolutePath());
            System.out.println("failed to remove tmp directory due to earlier error.");
        }
    }

    public void testFromOneTwoFifteen() throws Exception
    {
        importAndUpgradeTest("0102015000");
    }

    public void importAndUpgradeTest(String build) throws Exception
    {
        String db = System.getenv("PULSE_DB");
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
        configDir.mkdir();
        FileSystemUtils.copy(configDir, configFile);

        MutableConfiguration configuration = new MutableConfiguration();
        File mappingsDir = new File(buildDir, "mappings");
        String[] mappings = mappingsDir.list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".hbm.xml");
            }
        });

        for (String mapping : mappings)
        {
            Resource resource = new FileSystemResource(new File(mappingsDir, mapping));
            configuration.addInputStream(resource.getInputStream());
        }

        configuration.setProperty("hibernate.dialect", dialect);
        TransferAPI transferAPI = new TransferAPI();
        transferAPI.restore(configuration, new File(buildDir, "dump.xml"), dataSource);

        setupServerProperties(build);
        System.setProperty("bootstrap", "com/zutubi/pulse/bootstrap/ideaBootstrapContext.xml");
        runUpgrade(build);
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
        configDir.mkdir();
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
        System.setProperty("bootstrap", "com/zutubi/pulse/bootstrap/ideaBootstrapContext.xml");

        // extract zip file.
        InputStream is = null;
        try
        {
            File data = new File(dataArea, "pulse-1.1.0-data.zip");
            is = new FileInputStream(data);

            assertNotNull(is);
            FileSystemUtils.extractZip(new ZipInputStream(is), tmpDir);
        }
        finally
        {
            IOUtils.close(is);
        }

        runUpgrade("0101000000");
    }

    private void runUpgrade(String build) throws Exception
    {
        // start pulse using the extracted data directory.
        StartCommand start = new StartCommand();
        assertEquals(0, start.execute(getBootContext("start", "-p", "8990", "-d", tmpDir.getAbsolutePath())));

        // now we need to go to the Web UI and wait.

        getTestContext().setBaseUrl("http://localhost:8990");
        beginAt("/");

        // check that we have received the upgrade preview, and that the data is as expected.
        assertTextPresent("Upgrade Preview");
        assertTextPresent(build);

        tester.submit("continue");

        // waiting..
        assertTextPresent("Upgrade Progress");

        pauseWhileMetaRefreshActive();

        assertTextPresent("Upgrade Complete");
        assertTextPresent("The upgrade has been successful");

        clickLinkWithText("continue");

        ShutdownCommand shutdown = new ShutdownCommand();
        shutdown.setExitJvm(false);
        assertEquals(0, shutdown.execute(getBootContext("shutdown", "-F", "true", "-p", "8990")));

        // allow time for the shutdown to complete.
        Thread.sleep(3000);
    }

    private BootContext getBootContext(String... args)
    {
        return new BootContext(null, args, null, null, null);
    }
}
