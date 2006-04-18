/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.test.PulseTestCase;

import java.io.File;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class HomeTest extends PulseTestCase
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

    public void testInitialiseHome() throws IOException
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
        assertTrue(new File(homeDir, Home.CONFIG_FILE_NAME).exists());
    }

    public void testVersionDetails() throws IOException
    {
        Home home = new Home(homeDir);
        home.init();

        Version v = Version.getVersion();
        Version homeVersion = home.getVersion();
        assertEquals(v.getBuildNumber(), homeVersion.getBuildNumber());
        assertEquals(v.getVersionNumber(), homeVersion.getVersionNumber());
    }
}
