package com.zutubi.pulse.acceptance;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.acceptance.forms.setup.PulseLicenseForm;
import com.zutubi.pulse.acceptance.forms.setup.SetPulseDataForm;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.bootstrap.conf.Config;
import com.zutubi.pulse.bootstrap.conf.FileConfig;
import com.zutubi.pulse.command.PingServerCommand;
import com.zutubi.pulse.command.ShutdownCommand;
import com.zutubi.pulse.command.StartCommand;
import com.zutubi.pulse.util.FileSystemUtils;
import junit.framework.TestCase;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class StartupShutdownAcceptanceTest extends TestCase
{
    private File tmpDir;
    private File dataDir;
    private File defaultDataDir;
    private File configFile;
    private File defaultConfigFile;

    private Properties sys;

    protected void setUp() throws Exception
    {
        sys = new Properties();
        sys.putAll(System.getProperties());

        // This system property is only required when running these tests within intelliJ
        // So, if this property has been set, leave it. If not, set it. This means that we need to set this property
        // when running accept.master.
        if (!System.getProperties().containsKey("bootstrap"))
        {
            System.setProperty("bootstrap", "com/zutubi/pulse/bootstrap/ideaBootstrapContext.xml");
        }

        // create a temporary user home.
        tmpDir = FileSystemUtils.createTempDirectory(getClass().getSimpleName() + ".", "." + getName());
        dataDir = new File(tmpDir, "data");
        configFile = new File(tmpDir, "crazy.config.file.name");

        File userHome = new File(tmpDir, "home");
        defaultDataDir = new File(userHome, "data");
        defaultConfigFile = new File(userHome, FileSystemUtils.composeFilename(".pulse", "config.properties"));

        System.setProperty("user.home", userHome.getAbsolutePath());
    }

    protected void tearDown() throws Exception
    {
        assertTrue(FileSystemUtils.removeDirectory(tmpDir));
        tmpDir = null;
        dataDir = null;
        defaultDataDir = null;
        configFile = null;
        defaultConfigFile = null;

        System.getProperties().clear();
        System.getProperties().putAll(sys);
    }

    /**
     * check that pulse starts up and shutdowns down correctly using pulses internal defaults.
     */
    public void testDefaultFirstTimeStartup() throws Exception
    {
        RuntimeContext ctx = new RuntimeContext();

        RuntimeContext fileCtx = new RuntimeContext("8080", "/");
        fileCtx.setDataDirectory(defaultDataDir.getAbsolutePath());

        RuntimeContext actualCtx = new RuntimeContext("8080", "/");
        actualCtx.setDataDirectory(defaultDataDir.getAbsolutePath());
        actualCtx.setExternalConfig(defaultConfigFile.getAbsolutePath());

        assertFirstTimeStartup(ctx, fileCtx, actualCtx);
    }

    public void testFirstTimeStartupWithCommandLineOptions() throws Exception
    {
        RuntimeContext ctx = new RuntimeContext();
        ctx.setPort("8081");
        ctx.setContextPath("/builder");

        RuntimeContext fileCtx = new RuntimeContext("8080", "/");
        fileCtx.setDataDirectory(defaultDataDir.getAbsolutePath());

        RuntimeContext actualCtx = new RuntimeContext("8081", "/builder");
        actualCtx.setDataDirectory(defaultDataDir.getAbsolutePath());
        actualCtx.setExternalConfig(defaultConfigFile.getAbsolutePath());

        assertFirstTimeStartup(ctx, fileCtx, actualCtx);
    }

    public void testFirstTimeStartupWithDataDirectorySpecified() throws Exception
    {
        RuntimeContext ctx = new RuntimeContext("8082", "/pulse");
        ctx.setDataDirectory(dataDir.getAbsolutePath());

        RuntimeContext fileCtx = new RuntimeContext("8080", "/");
        fileCtx.setDataDirectory(dataDir.getAbsolutePath());

        RuntimeContext actualCtx = new RuntimeContext("8082", "/pulse");
        actualCtx.setDataDirectory(dataDir.getAbsolutePath());
        actualCtx.setExternalConfig(defaultConfigFile.getAbsolutePath());

        assertFirstTimeStartup(ctx, fileCtx, actualCtx);
    }

    public void testFirstTimeStartupWithEverythingSpecified() throws Exception
    {
        RuntimeContext ctx = new RuntimeContext("8083", "/some/sort/of/crazy/context/path");
        ctx.setDataDirectory(dataDir.getAbsolutePath());
        ctx.setExternalConfig(configFile.getAbsolutePath());

        RuntimeContext fileCtx = new RuntimeContext("8080", "/");
        fileCtx.setDataDirectory(dataDir.getAbsolutePath());

        RuntimeContext actualCtx = new RuntimeContext("8083", "/some/sort/of/crazy/context/path");
        actualCtx.setDataDirectory(dataDir.getAbsolutePath());
        actualCtx.setExternalConfig(configFile.getAbsolutePath());

        assertFirstTimeStartup(ctx, fileCtx, actualCtx);
    }

    /**
     * Here, a first time startup assumes that the external config file does not exist.
     */
    private void assertFirstTimeStartup(RuntimeContext cmdCtx, RuntimeContext fileCtx, RuntimeContext actualCtx) throws Exception
    {
        assertServerNotAvailable(actualCtx);
        assertExternalConfigNotAvailable(actualCtx);

        assertStartServer(cmdCtx);
        assertServerAvailable(actualCtx);

        if (TextUtils.stringSet(cmdCtx.getConfiguredDataDirectory()))
        {
            assertPromptForLicense(actualCtx);
        }
        else
        {
            fileCtx.setDataDirectory(assertPromptForPulseDataDirectory(actualCtx));
        }

        // verify that the pulse config file is written to the user home.
        assertExternalConfigAvailable(actualCtx);
        assertExternalConfigContents(actualCtx, fileCtx);

        // now lets shutdown the server.
        assertShutdownServer(cmdCtx);
        assertServerNotAvailable(actualCtx);
    }

    public void testDefaultSecondTimeStartup() throws Exception
    {
        RuntimeContext ctx = new RuntimeContext();

        // create external config file setting the values we expect.
        Config config = new FileConfig(defaultConfigFile);
        config.setProperty(SystemConfiguration.WEBAPP_PORT, "8080");
        config.setProperty(SystemConfiguration.CONTEXT_PATH, "/");
        config.setProperty(SystemConfiguration.PULSE_DATA, defaultDataDir.getAbsolutePath());

        // the file runtime context should mirror the config file.
        RuntimeContext fileCtx = new RuntimeContext("8080", "/");
        fileCtx.setDataDirectory(defaultDataDir.getAbsolutePath());

        // the actual runtime context.
        RuntimeContext actualCtx = new RuntimeContext("8080", "/");
        actualCtx.setDataDirectory(defaultDataDir.getAbsolutePath());
        actualCtx.setExternalConfig(defaultConfigFile.getAbsolutePath());

        assertSecondTimeStartup(ctx, fileCtx, actualCtx);
    }

    public void testSecondTimeStartupWithCommandLine() throws Exception
    {
        RuntimeContext ctx = new RuntimeContext("8081", "/sweet");

        // the file runtime context should mirror the config file.
        RuntimeContext fileCtx = new RuntimeContext("8080", "/");
        fileCtx.setDataDirectory(defaultDataDir.getAbsolutePath());
        fileCtx.setExternalConfig(defaultConfigFile.getAbsolutePath());
        writeToConfigFile(fileCtx);

        // the actual runtime context.
        RuntimeContext actualCtx = new RuntimeContext("8081", "/sweet");
        actualCtx.setDataDirectory(defaultDataDir.getAbsolutePath());
        actualCtx.setExternalConfig(defaultConfigFile.getAbsolutePath());

        assertSecondTimeStartup(ctx, fileCtx, actualCtx);
    }

    public void testSecondTimeStartupWithEditedConfig() throws Exception
    {
        RuntimeContext ctx = new RuntimeContext();

        // the file runtime context should mirror the config file.
        RuntimeContext fileCtx = new RuntimeContext("8083", "/solid");
        fileCtx.setDataDirectory(defaultDataDir.getAbsolutePath());
        fileCtx.setExternalConfig(defaultConfigFile.getAbsolutePath());
        writeToConfigFile(fileCtx);

        // the actual runtime context.
        RuntimeContext actualCtx = new RuntimeContext("8083", "/solid");
        actualCtx.setDataDirectory(defaultDataDir.getAbsolutePath());
        actualCtx.setExternalConfig(defaultConfigFile.getAbsolutePath());

        assertSecondTimeStartup(ctx, fileCtx, actualCtx);
    }

    public void testSecondTimeStartupWithCustomEditedConfig() throws Exception
    {
        // the file runtime context should mirror the config file.
        RuntimeContext fileCtx = new RuntimeContext("8086", "/domain/name/anyone");
        fileCtx.setDataDirectory(dataDir.getAbsolutePath());
        fileCtx.setExternalConfig(configFile.getAbsolutePath());
        writeToConfigFile(fileCtx);

        RuntimeContext ctx = new RuntimeContext();
        ctx.setExternalConfig(configFile.getAbsolutePath());

        // the actual runtime context.
        RuntimeContext actualCtx = new RuntimeContext("8086", "/domain/name/anyone");
        actualCtx.setDataDirectory(dataDir.getAbsolutePath());
        actualCtx.setExternalConfig(configFile.getAbsolutePath());

        assertSecondTimeStartup(ctx, fileCtx, actualCtx);
    }

    private void writeToConfigFile(RuntimeContext fileCtx)
    {
        // create external config file setting the values we expect.
        Config config = new FileConfig(fileCtx.getExternalConfigFile());
        config.setProperty(SystemConfiguration.WEBAPP_PORT, fileCtx.getPort());
        config.setProperty(SystemConfiguration.CONTEXT_PATH, fileCtx.getContextPath());
        config.setProperty(SystemConfiguration.PULSE_DATA, fileCtx.getConfiguredDataDirectory());
    }

    /**
     *
     */
    public void assertSecondTimeStartup(RuntimeContext cmdCtx, RuntimeContext fileContext, RuntimeContext actualCtx) throws Exception
    {
        assertExternalConfigAvailable(actualCtx);
        assertServerNotAvailable(actualCtx);

        assertStartServer(cmdCtx);
        assertServerAvailable(actualCtx);

        // always expected a license prompt, never a data prompt since the data directory is located in the config file.
        assertPromptForLicense(actualCtx);

        // verify that the pulse config file is written to the user home.
        assertExternalConfigAvailable(actualCtx);
        assertExternalConfigContents(actualCtx, fileContext);

        // now lets shutdown the server.
        assertShutdownServer(cmdCtx);
        assertServerNotAvailable(actualCtx);
    }

    private String assertPromptForPulseDataDirectory(RuntimeContext ctx)
    {
        WebTester tester = ctx.initWebTester();

        SetPulseDataForm form = new SetPulseDataForm(tester);
        form.assertFormPresent();

        String dataDir = form.getFormValues()[0];

        // set the data directory
        form.next();

        return dataDir;
    }

    private void assertPromptForLicense(RuntimeContext ctx)
    {
        WebTester tester = ctx.initWebTester();

        PulseLicenseForm form = new PulseLicenseForm(tester);
        form.assertFormPresent();
    }

    private void assertServerNotAvailable(RuntimeContext ctx)
    {
        PingServerCommand ping = new PingServerCommand();
        ping.setBaseUrl(ctx.getBaseUrl());
        assertEquals(2, ping.execute());
    }

    private void assertServerAvailable(RuntimeContext ctx)
    {
        PingServerCommand ping = new PingServerCommand();
        ping.setBaseUrl(ctx.getBaseUrl());
        assertEquals(0, ping.execute());
    }

    private void assertStartServer(RuntimeContext ctx) throws ParseException
    {
        StartCommand start = new StartCommand();
        List<String> args = new LinkedList<String>();
        if (TextUtils.stringSet(ctx.getPort()))
        {
            args.add("-p");
            args.add(ctx.getPort());
        }
        if (TextUtils.stringSet(ctx.getContextPath()))
        {
            args.add("-c");
            args.add(ctx.getContextPath());
        }
        if (TextUtils.stringSet(ctx.getConfiguredDataDirectory()))
        {
            args.add("-d");
            args.add(ctx.getConfiguredDataDirectory());
        }
        if (TextUtils.stringSet(ctx.getExternalConfig()))
        {
            args.add("-f");
            args.add(ctx.getExternalConfig());
        }
        start.parse(args.toArray(new String[args.size()]));
        assertEquals(0, start.execute());
    }

    private void assertShutdownServer(RuntimeContext ctx) throws ParseException, InterruptedException
    {
        ShutdownCommand shutdown = new ShutdownCommand();
        List<String> args = new LinkedList<String>();
        if (TextUtils.stringSet(ctx.getPort()))
        {
            args.add("-p");
            args.add(ctx.getPort());
        }
        if (TextUtils.stringSet(ctx.getContextPath()))
        {
            args.add("-c");
            args.add(ctx.getContextPath());
        }
        if (TextUtils.stringSet(ctx.getExternalConfig()))
        {
            args.add("-f");
            args.add(ctx.getExternalConfig());
        }
        shutdown.parse(args.toArray(new String[args.size()]));
        shutdown.setExitJvm(false);
        assertEquals(0, shutdown.execute());

        Thread.sleep(2000); // give pulse a chance to shutdown.
    }

    private void assertExternalConfigNotAvailable(RuntimeContext ctx)
    {
        assertFalse(ctx.getExternalConfigFile().exists());
    }

    private void assertExternalConfigAvailable(RuntimeContext ctx)
    {
        assertTrue(ctx.getExternalConfigFile().isFile());
    }

    private void assertExternalConfigContents(RuntimeContext actualContext, RuntimeContext fileCtx)
    {
        File externalConfigFile = actualContext.getExternalConfigFile();
        Config config = new FileConfig(externalConfigFile);
        assertTrue(config.hasProperty(SystemConfiguration.PULSE_DATA));
        assertTrue(config.hasProperty(SystemConfiguration.WEBAPP_PORT));
        assertTrue(config.hasProperty(SystemConfiguration.CONTEXT_PATH));

        // these are the expected contents of the external config file generated by the pulse instance.
        // If the file was already there (updated by the user), then these values may vary.
        assertEquals(fileCtx.getPort(), config.getProperty(SystemConfiguration.WEBAPP_PORT));
        assertEquals(fileCtx.getContextPath(), config.getProperty(SystemConfiguration.CONTEXT_PATH));
        assertEquals(fileCtx.getConfiguredDataDirectory(), config.getProperty(SystemConfiguration.PULSE_DATA));
    }

    private class RuntimeContext
    {
        private String contextPath;

        private String port;

        private String externalConfigLocation;

        private String configuredDataDirectory;

        public RuntimeContext()
        {
        }

        public RuntimeContext(String port, String contextPath)
        {
            this.contextPath = contextPath;
            this.port = port;
        }

        public String getContextPath()
        {
            return contextPath;
        }

        public String getPort()
        {
            return port;
        }

        public void setContextPath(String contextPath)
        {
            this.contextPath = contextPath;
        }

        public void setPort(String port)
        {
            this.port = port;
        }

        public String getExternalConfig()
        {
            return externalConfigLocation;
        }

        public File getExternalConfigFile()
        {
            return new File(externalConfigLocation);
        }

        public void setExternalConfig(String externalConfigLocation)
        {
            this.externalConfigLocation = externalConfigLocation;
        }

        public void setDataDirectory(String configuredDataDirectory)
        {
            this.configuredDataDirectory = configuredDataDirectory;
        }

        public String getConfiguredDataDirectory()
        {
            return configuredDataDirectory;
        }

        public String getBaseUrl()
        {
            return "http://localhost:" + getPort() + getContextPath();
        }

        public WebTester initWebTester()
        {
            WebTester tester = new WebTester();
            tester.getTestContext().setBaseUrl(getBaseUrl());
            tester.beginAt("/");
            return tester;
        }
    }
}
