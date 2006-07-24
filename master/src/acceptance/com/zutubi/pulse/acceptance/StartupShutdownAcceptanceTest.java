package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.command.PingServerCommand;
import com.zutubi.pulse.command.ShutdownCommand;
import com.zutubi.pulse.command.StartCommand;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.bootstrap.conf.FileConfig;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.TestSetup;

import java.io.File;
import java.util.Properties;

/**
 * <class-comment/>
 */
public class StartupShutdownAcceptanceTest extends TestCase
{
    private File userHome;

    public static Test suite()
    {
        TestSuite testSuite = new TestSuite(StartupShutdownAcceptanceTest.class);
        return new PreserveSystemProperties(testSuite);
    }

    protected void setUp() throws Exception
    {
        // This system property is only required when running these tests within intelliJ
        // So, if this property has been set, leave it. If not, set it. This means that we need to set this property
        // when running accept.master.
        if (!System.getProperties().containsKey("bootstrap"))
        {
            System.setProperty("bootstrap", "com/zutubi/pulse/bootstrap/ideaBootstrapContext.xml");
        }

        // create a temporary user home.
        userHome = FileSystemUtils.createTempDirectory(getName(), "");
        System.setProperty("user.home", userHome.getAbsolutePath());
    }

    protected void tearDown() throws Exception
    {
        assertTrue(FileSystemUtils.removeDirectory(userHome));
    }

    /**
     * check that pulse starts up and shutdowns down correctly using pulses internal defaults.
     */
    public void testDefaultPulseStartupAndShutdown() throws InterruptedException
    {
        PingServerCommand ping = new PingServerCommand();
        ping.setBaseUrl("http://localhost:8080");
        assertEquals(2, ping.execute()); // assert that pulse is not available.

        StartCommand start = new StartCommand();
        assertEquals(0, start.execute());

        assertEquals(0, ping.execute()); // assert that pulse is available.

        ShutdownCommand shutdown = new ShutdownCommand();
        shutdown.setExitJvm(false);
        assertEquals(0, shutdown.execute());

        Thread.sleep(2000); // give pulse a chance to shutdown.
        assertEquals(2, ping.execute()); // assert that pulse has shutdown.
    }

    /**
     * Add configuration parameters to the 'user.home' directory and ensure that these
     * are picked up during startup.
     */
    //@Required("tmpdir")
    public void testConfigViaUserHome() throws Exception
    {
        // create a pulse.properties file in the user.home/.pulse/ directory.
        FileConfig conf = new FileConfig(new File(userHome, ".pulse" + File.separator + "pulse.properties"));
        conf.setProperty(SystemConfiguration.CONTEXT_PATH, "user");
        conf.setProperty(SystemConfiguration.WEBAPP_PORT, "8023");

        PingServerCommand ping = new PingServerCommand();
        ping.parse("http://localhost:8023/user");

        assertEquals(2, ping.execute()); // assert that pulse is not available.

        StartCommand start = new StartCommand();
        assertEquals(0, start.execute());

        assertEquals(0, ping.execute()); // assert that pulse is available.

        ShutdownCommand shutdown = new ShutdownCommand();
        shutdown.setExitJvm(false);
        assertEquals(0, shutdown.execute());

        Thread.sleep(2000); // give pulse a chance to shutdown.
        assertEquals(2, ping.execute()); // assert that pulse has shutdown.
    }

    /**
     * Check that the command line overrides the user properties file. We already know that the user
     * properties file overrides the defaults so no need to check that.
     */
    public void testConfigOverride() throws Exception
    {
        // create a pulse.properties file in the user.home/.pulse/ directory.
        FileConfig conf = new FileConfig(new File(userHome, ".pulse" + File.separator + "pulse.properties"));
        conf.setProperty(SystemConfiguration.CONTEXT_PATH, "user");
        conf.setProperty(SystemConfiguration.WEBAPP_PORT, "8023");

        PingServerCommand ping = new PingServerCommand();
        ping.parse("http://localhost:8025/user");

        assertEquals(2, ping.execute()); // assert that pulse is not available.

        StartCommand start = new StartCommand();
        start.parse("-p", "8025");
        assertEquals(0, start.execute());

        assertEquals(0, ping.execute()); // assert that pulse is available.

        ShutdownCommand shutdown = new ShutdownCommand();
        shutdown.parse("-p", "8025");
        shutdown.setExitJvm(false);
        assertEquals(0, shutdown.execute());

        Thread.sleep(2000); // give pulse a chance to shutdown.
        assertEquals(2, ping.execute()); // assert that pulse has shutdown.
    }

    /**
     * Add configuration parameters to an arbitrary file and ensure that these are
     * picked up during startup with the appropriate command line options.
     */
    public void testConfigViaCustomConfigFile()
    {
        //TODO: support this.
    }

    /**
     * Check that pulse starts and stops correctly based on parameters provided
     * on the command line.
     */
    public void testConfigViaCommandLine() throws Exception
    {
        PingServerCommand ping = new PingServerCommand();
        ping.parse("http://localhost:8081/build");

        assertEquals(2, ping.execute()); // assert that pulse is not available.

        StartCommand start = new StartCommand();
        start.parse("-p", "8081", "-c", "build");
        assertEquals(0, start.execute());

        assertEquals(0, ping.execute()); // assert that pulse is available.

        ShutdownCommand shutdown = new ShutdownCommand();
        shutdown.parse("-p", "8081", "-c", "build");
        shutdown.setExitJvm(false);
        assertEquals(0, shutdown.execute());

        Thread.sleep(2000); // give pulse a chance to shutdown.
        assertEquals(2, ping.execute()); // assert that pulse has shutdown.
    }

    public void testAgentStartupAndShutdown()
    {
        // we can not test this here since the classpath is for the  master, not the agent.
    }

    public static class PreserveSystemProperties extends TestSetup
    {
        private Properties sys;
        public PreserveSystemProperties(Test test)
        {
            super(test);
        }

        protected void setUp() throws Exception
        {
            super.setUp();

            sys = new Properties();
            sys.putAll(System.getProperties());
        }

        protected void tearDown() throws Exception
        {
            System.getProperties().clear();
            System.getProperties().putAll(sys);

            super.tearDown();
        }
    }
}
