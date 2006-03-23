package com.cinnamonbob.bootstrap;

import com.cinnamonbob.Version;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.test.BobTestCase;

import java.io.File;

/**
 * <class-comment/>
 */
public class HomeTest extends BobTestCase
{
    private File homeDir;

    public HomeTest()
    {
    }

    public HomeTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        homeDir = FileSystemUtils.createTempDirectory(HomeTest.class.getName(), "");
        homeDir.delete();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(homeDir);

        super.tearDown();
    }

    public void testInitialiseHome()
    {
        Home home = new Home(homeDir);

        // ensure that this works when the home directory does not exist.
        assertFalse(home.isInitialised());

        // ensure that this works when the home directory exists.
        assertTrue(homeDir.mkdirs());
        assertFalse(home.isInitialised());

        home.init();

        assertTrue(home.isInitialised());
        assertTrue(homeDir.exists());
        assertTrue(new File(homeDir, "version.properties").exists());
    }

    public void testVersionDetails()
    {
        Home home = new Home(homeDir);
        home.init();

        Version v = Version.getVersion();
        Version homeVersion = home.getHomeVersion();
        assertEquals(v.getBuildNumber(), homeVersion.getBuildNumber());
        assertEquals(v.getVersionNumber(), homeVersion.getVersionNumber());
    }
}
