package com.cinnamonbob.bootstrap;

import junit.framework.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.cinnamonbob.core.util.FileSystemUtils;

/**
 * <class-comment/>
 */
public class DefaultConfigurationManagerTest extends TestCase
{
    private File bobInstall;
    private File bobHome;

    private DefaultConfigurationManager configManager;

    public DefaultConfigurationManagerTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        bobInstall = FileSystemUtils.createTempDirectory(this.getClass().getName(), "bob.install");
        bobHome = FileSystemUtils.createTempDirectory(this.getClass().getName(), "bob.home");

        configManager = new DefaultConfigurationManager();
        configManager.setSystemPaths(new DefaultSystemPaths(bobInstall));
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.
        configManager = null;

        if (!FileSystemUtils.removeDirectory(bobInstall))
        {
            throw new IOException("Unable to delete " + bobInstall);
        }
        if (!FileSystemUtils.removeDirectory(bobHome))
        {
            throw new IOException("Unable to delete " + bobHome);
        }

        super.tearDown();
    }

    public void testBobHome()
    {
        assertNull(configManager.getBobHome());
        configManager.setBobHome(bobHome);
        assertEquals(bobHome, configManager.getBobHome());
    }

/*
    public void testOverrideBobHomeWithSystemProperty()
    {
        configManager.setBobHome(bobHome);
        assertEquals(bobHome, configManager.getBobHome());

        File randomFile = new File("some/random/path");

        Properties sys = new Properties();
        sys.setProperty(InitConfiguration.BOB_HOME, randomFile.getAbsolutePath());
        // put the override in place.
        configManager.setSystemProperties(sys);

        assertEquals(randomFile, configManager.getBobHome());
    }
*/
}