package com.zutubi.pulse.acceptance;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.acceptance.forms.setup.PulseLicenseForm;
import com.zutubi.pulse.acceptance.forms.setup.SetPulseDataForm;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.PingServerCommand;
import com.zutubi.pulse.command.ShutdownCommand;
import com.zutubi.pulse.command.StartCommand;
import com.zutubi.pulse.config.Config;
import com.zutubi.pulse.config.FileConfig;
import com.zutubi.pulse.util.FileSystemUtils;
import junit.framework.TestCase;
import net.sourceforge.jwebunit.WebTester;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
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
        // This system property is only required when running these tests within intelliJ
        // So, if this property has been set, leave it. If not, set it. This means that we need to set this property
        // when running accept.master.
        if (!System.getProperties().containsKey("bootstrap"))
        {
            System.setProperty("bootstrap", "com/zutubi/pulse/bootstrap/ideaBootstrapContext.xml");
        }
        sys = new Properties();
        sys.putAll(System.getProperties());

        // create a temporary user home.
        tmpDir = FileSystemUtils.createTempDir(getClass().getSimpleName() + ".", "." + getName());
        dataDir = new File(tmpDir, "data");
        configFile = new File(tmpDir, "crazy.config.file.name");

        File userHome = new File(tmpDir, "home");
        defaultDataDir = new File(userHome, "data");
        defaultConfigFile = new File(userHome, FileSystemUtils.composeFilename(".pulse", "config.properties"));

        System.setProperty("user.home", userHome.getAbsolutePath());
    }

    protected void tearDown() throws Exception
    {
        assertTrue(FileSystemUtils.rmdir(tmpDir));
        tmpDir = null;
        dataDir = null;
        defaultDataDir = null;
        configFile = null;
        defaultConfigFile = null;

        resetSystemProperties();
    }

    private void resetSystemProperties()
    {
        System.getProperties().clear();
        System.getProperties().putAll(sys);
    }

    /**
     * check that pulse starts up and shutdowns down correctly using pulses internal defaults.
     */
    public void testDefaultFirstTimeStartup() throws Exception
    {
        // we are using the default runtime context.
        RuntimeContext commandline = new RuntimeContext();

        // we are expecting the following file context.
        RuntimeContext file = new RuntimeContext("8080", "/");
        file.setDataDirectory(defaultDataDir.getAbsolutePath());

        // we are expecting the actual server context to be the following:
        RuntimeContext expected = new RuntimeContext("8080", "/");
        expected.setDataDirectory(defaultDataDir.getAbsolutePath());
        expected.setExternalConfig(defaultConfigFile.getAbsolutePath());

        // assert that things are expected in the situation where we are starting the
        // server for the first time.
        assertFirstTimeStartup(commandline, file, expected);
    }

    public void testFirstTimeStartupWithCommandLineOptions() throws Exception
    {
        RuntimeContext commandline = new RuntimeContext();
        commandline.setPort("8081");
        commandline.setContextPath("/builder");

        RuntimeContext file = new RuntimeContext("8080", "/");
        file.setDataDirectory(defaultDataDir.getAbsolutePath());

        RuntimeContext expected = new RuntimeContext("8081", "/builder");
        expected.setDataDirectory(defaultDataDir.getAbsolutePath());
        expected.setExternalConfig(defaultConfigFile.getAbsolutePath());

        assertFirstTimeStartup(commandline, file, expected);
    }

    public void testFirstTimeStartupWithDataDirectorySpecified() throws Exception
    {
        RuntimeContext commandline = new RuntimeContext("8082", "/pulse");
        commandline.setDataDirectory(dataDir.getAbsolutePath());

        RuntimeContext file = new RuntimeContext("8080", "/");
        file.setDataDirectory(dataDir.getAbsolutePath());

        RuntimeContext expected = new RuntimeContext("8082", "/pulse");
        expected.setDataDirectory(dataDir.getAbsolutePath());
        expected.setExternalConfig(defaultConfigFile.getAbsolutePath());

        assertFirstTimeStartup(commandline, file, expected);
    }

    public void testFirstTimeStartupWithEverythingSpecified() throws Exception
    {
        RuntimeContext commandline = new RuntimeContext("8083", "/some/sort/of/crazy/context/path");
        commandline.setDataDirectory(dataDir.getAbsolutePath());
        commandline.setExternalConfig(configFile.getAbsolutePath());

        RuntimeContext file = new RuntimeContext("8080", "/");
        file.setDataDirectory(dataDir.getAbsolutePath());

        RuntimeContext expected = new RuntimeContext("8083", "/some/sort/of/crazy/context/path");
        expected.setDataDirectory(dataDir.getAbsolutePath());
        expected.setExternalConfig(configFile.getAbsolutePath());

        assertFirstTimeStartup(commandline, file, expected);
    }

    /**
     * Here, a first time startup assumes that the external config file does not exist.
     *
     * @param commandline is the command line context. This is what should be used to run a command.
     * @param file is the expected context stored in the file.
     * @param expected is the actual server context. This is what is used to communicate with the server.
     */
    private void assertFirstTimeStartup(RuntimeContext commandline, RuntimeContext file, RuntimeContext expected) throws Exception
    {
        assertServerNotAvailable(expected);
        assertExternalConfigNotAvailable(expected);

        assertStartServer(commandline);
        assertServerAvailable(expected);

        if (TextUtils.stringSet(commandline.getConfiguredDataDirectory()))
        {
            assertPromptForLicense(expected);
        }
        else
        {
            file.setDataDirectory(assertPromptForPulseDataDirectory(expected));
        }

        // verify that the pulse config file is written to the user home.
        assertExternalConfigAvailable(expected.getExternalConfigFile());
        assertExternalConfigContents(expected.getExternalConfigFile(), file);

        // ensure the command line (system properties) is cleaned up.
        resetSystemProperties();

        // now lets shutdown the server.
        assertShutdownServer(commandline);
        assertServerNotAvailable(expected);
    }

    public void testDefaultSecondTimeStartup() throws Exception
    {
        RuntimeContext commandline = new RuntimeContext();

        // the file runtime context should mirror the config file.
        RuntimeContext file = new RuntimeContext("8080", "/");
        file.setDataDirectory(defaultDataDir.getAbsolutePath());
        writeToConfigFile(defaultConfigFile, file);

        // the actual runtime context.
        RuntimeContext expected = new RuntimeContext("8080", "/");
        expected.setDataDirectory(defaultDataDir.getAbsolutePath());
        expected.setExternalConfig(defaultConfigFile.getAbsolutePath());

        assertSecondTimeStartup(commandline, file, expected);
    }

    public void testSecondTimeStartupWithCommandLine() throws Exception
    {
        RuntimeContext commandline = new RuntimeContext("8081", "/sweet");

        // the file runtime context should mirror the config file.
        RuntimeContext file = new RuntimeContext("8080", "/");
        file.setDataDirectory(defaultDataDir.getAbsolutePath());
        writeToConfigFile(defaultConfigFile, file);

        // the actual runtime context.
        RuntimeContext expected = new RuntimeContext("8081", "/sweet");
        expected.setDataDirectory(defaultDataDir.getAbsolutePath());
        expected.setExternalConfig(defaultConfigFile.getAbsolutePath());

        assertSecondTimeStartup(commandline, file, expected);
    }

    public void testSecondTimeStartupWithEditedConfig() throws Exception
    {
        RuntimeContext commandline = new RuntimeContext();

        // the file runtime context should mirror the config file.
        RuntimeContext file = new RuntimeContext("8083", "/solid");
        file.setDataDirectory(defaultDataDir.getAbsolutePath());
        writeToConfigFile(defaultConfigFile, file);

        // the actual runtime context.
        RuntimeContext expected = new RuntimeContext("8083", "/solid");
        expected.setDataDirectory(defaultDataDir.getAbsolutePath());
        expected.setExternalConfig(defaultConfigFile.getAbsolutePath());

        assertSecondTimeStartup(commandline, file, expected);
    }

    public void testSecondTimeStartupWithCustomEditedConfig() throws Exception
    {
        RuntimeContext commandline = new RuntimeContext();
        commandline.setExternalConfig(configFile.getAbsolutePath());

        // the file runtime context should mirror the config file.
        RuntimeContext file = new RuntimeContext("8086", "/domain/name/anyone");
        file.setDataDirectory(dataDir.getAbsolutePath());
        writeToConfigFile(configFile, file);

        // the actual runtime context.
        RuntimeContext expected = new RuntimeContext("8086", "/domain/name/anyone");
        expected.setDataDirectory(dataDir.getAbsolutePath());
        expected.setExternalConfig(configFile.getAbsolutePath());

        assertSecondTimeStartup(commandline, file, expected);
    }

    // test the shutdown command to ensure that

    /**
     * Test that the shutdown command is able to shutdown a server configured
     * to run on non-standard port and context path.
     *
     * This is more a test of the AdminCommand being able to accurately locate
     * the running server than the shutdown command being able to shutdown a
     * server.
     *
     * @throws Exception if an unexpected error occurs.
     */
    public void testThatShutdownWorksForNonStandardDeployment() throws Exception
    {
        RuntimeContext startupCommandLine = new RuntimeContext("8083", "/some/sort/of/crazy/context/path");
        startupCommandLine.setDataDirectory(dataDir.getAbsolutePath());
        startupCommandLine.setExternalConfig(configFile.getAbsolutePath());

        assertServerNotAvailable(startupCommandLine);
        assertStartServer(startupCommandLine);
        assertServerAvailable(startupCommandLine);

        resetSystemProperties();

        // now lets shutdown the server using the default context. This checks
        // that the runtime context is remembered and used for admin functions.
        RuntimeContext shutdownCommandLine = new RuntimeContext();
        assertShutdownServer(shutdownCommandLine);
        
        assertServerNotAvailable(startupCommandLine);
    }

    /**
     * This is the scenario when a user is changing the configuration of a
     * running pulse instance.  We need to ensure that the file context is not
     * relied upon to interact with the running server.
     *
     * @throws Exception if an unexpected error occurs.
     */
    public void testShutdownWorksForIncorrectFileConfig() throws Exception
    {
        RuntimeContext startupCommandLine = new RuntimeContext("8083", "/some/sort/of/crazy/context/path");
        startupCommandLine.setDataDirectory(dataDir.getAbsolutePath());
        startupCommandLine.setExternalConfig(configFile.getAbsolutePath());

        assertServerNotAvailable(startupCommandLine);
        assertStartServer(startupCommandLine);
        assertServerAvailable(startupCommandLine);

        resetSystemProperties();

        // now we set the new startup configuration while the server is still
        // running.
        RuntimeContext file = new RuntimeContext("8086", "/domain/name/anyone");
        file.setDataDirectory(dataDir.getAbsolutePath());
        writeToConfigFile(configFile, file);

        // test that the shutdown command is still able to shutdown the runnign server.
        RuntimeContext shutdownCommandLine = new RuntimeContext();
        assertShutdownServer(shutdownCommandLine);

        assertServerNotAvailable(startupCommandLine);
    }

    private void writeToConfigFile(File externalConfigFile, RuntimeContext fileCtx)
    {
        // create external config file setting the values we expect.
        Config config = new FileConfig(externalConfigFile);
        config.setProperty(SystemConfiguration.WEBAPP_PORT, fileCtx.getPort());
        config.setProperty(SystemConfiguration.CONTEXT_PATH, fileCtx.getContextPath());
        config.setProperty(SystemConfiguration.PULSE_DATA, fileCtx.getConfiguredDataDirectory());
    }

    /**
     *
     * @throws Exception if an unexpected error occurs.
     */
    public void assertSecondTimeStartup(RuntimeContext commandline, RuntimeContext file, RuntimeContext expected) throws Exception
    {
        assertExternalConfigAvailable(expected.getExternalConfigFile());
        assertServerNotAvailable(expected);

        assertStartServer(commandline);
        assertServerAvailable(expected);

        // always expected a license prompt, never a data prompt since the data directory is located in the config file.
        assertPromptForLicense(expected);

        // verify that the pulse config file is written to the user home.
        assertExternalConfigAvailable(expected.getExternalConfigFile());
        assertExternalConfigContents(expected.getExternalConfigFile(), file);

        // now lets shutdown the server.
        assertShutdownServer(commandline);
        assertServerNotAvailable(expected);
    }

    private String assertPromptForPulseDataDirectory(RuntimeContext expected)
    {
        WebTester tester = expected.initWebTester();

        SetPulseDataForm form = new SetPulseDataForm(tester);
        form.assertFormPresent();

        String dataDir = form.getFormValues()[0];

        // set the data directory
        form.next();

        return dataDir;
    }

    private void assertPromptForLicense(RuntimeContext expected)
    {
        WebTester tester = expected.initWebTester();

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

    private void assertStartServer(RuntimeContext commandline) throws ParseException
    {
        StartCommand start = new StartCommand();
        List<String> args = new LinkedList<String>();

        args.add("start");
        if (TextUtils.stringSet(commandline.getPort()))
        {
            args.add("-p");
            args.add(commandline.getPort());
        }
        if (TextUtils.stringSet(commandline.getContextPath()))
        {
            args.add("-c");
            args.add(commandline.getContextPath());
        }
        if (TextUtils.stringSet(commandline.getConfiguredDataDirectory()))
        {
            args.add("-d");
            args.add(commandline.getConfiguredDataDirectory());
        }
        if (TextUtils.stringSet(commandline.getExternalConfig()))
        {
            args.add("-f");
            args.add(commandline.getExternalConfig());
        }
        assertEquals(0, start.execute(getBootContext(args)));
    }

    private void assertShutdownServer(RuntimeContext commandline) throws ParseException, InterruptedException, IOException
    {
        ShutdownCommand shutdown = new ShutdownCommand();
        List<String> args = new LinkedList<String>();
        args.add("shutdown");
        if (TextUtils.stringSet(commandline.getPort()))
        {
            args.add("-p");
            args.add(commandline.getPort());
        }
        if (TextUtils.stringSet(commandline.getContextPath()))
        {
            args.add("-c");
            args.add(commandline.getContextPath());
        }
        if (TextUtils.stringSet(commandline.getExternalConfig()))
        {
            args.add("-f");
            args.add(commandline.getExternalConfig());
        }
        shutdown.setExitJvm(false);
        assertEquals(0, shutdown.execute(getBootContext(args)));

        Thread.sleep(2000); // give pulse a chance to shutdown.
    }

    private BootContext getBootContext(List<String> args)
    {
        return new BootContext(null, args.toArray(new String[args.size()]), null, null, null);
    }

    private void assertExternalConfigNotAvailable(RuntimeContext ctx)
    {
        assertFalse(ctx.getExternalConfigFile().exists());
    }

    private void assertExternalConfigAvailable(File f)
    {
        assertTrue(f.isFile());
    }

    private void assertExternalConfigContents(File externalConfigFile, RuntimeContext expected)
    {
        Config config = new FileConfig(externalConfigFile);
        assertTrue(config.hasProperty(SystemConfiguration.PULSE_DATA));
        assertTrue(config.hasProperty(SystemConfiguration.WEBAPP_PORT));
        assertTrue(config.hasProperty(SystemConfiguration.CONTEXT_PATH));

        // these are the expected contents of the external config file generated by the pulse instance.
        // If the file was already there (updated by the user), then these values may vary.
        assertEquals(expected.getPort(), config.getProperty(SystemConfiguration.WEBAPP_PORT));
        assertEquals(expected.getContextPath(), config.getProperty(SystemConfiguration.CONTEXT_PATH));

        assertEquals(expected.getConfiguredDataDirectory(), config.getProperty(SystemConfiguration.PULSE_DATA));
    }

    /**
     * The runtime context object defines the variables used by pulse when starting up to determine
     * its runtime configuration.
     * 
     */
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
