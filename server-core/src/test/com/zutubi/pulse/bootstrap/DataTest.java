package com.zutubi.pulse.bootstrap;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.test.PulseTestCase;

import java.io.File;
import java.io.IOException;

/**
 * <class-comment/>
 */
public class DataTest extends PulseTestCase
{
    private File dataDir;

    public DataTest()
    {
    }

    public DataTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        dataDir = FileSystemUtils.createTempDirectory(DataTest.class.getName(), "");
        dataDir.delete();
    }

    protected void tearDown() throws Exception
    {
        removeDirectory(dataDir);

        super.tearDown();
    }

    public void testInitialiseData() throws IOException
    {
        Data data = new Data(dataDir);

        // ensure that this works when the data directory does not exist.
        assertFalse(data.isInitialised());

        // ensure that this works when the data directory exists.
        assertTrue(dataDir.mkdirs());
        assertFalse(data.isInitialised());

        data.init();

        assertTrue(data.isInitialised());
        assertTrue(dataDir.exists());
        assertTrue(new File(dataDir, Data.CONFIG_FILE_NAME).exists());
    }

    public void testVersionDetails() throws IOException
    {
        Data data = new Data(dataDir);
        data.init();

        Version v = Version.getVersion();
        Version dataVersion = data.getVersion();
        assertEquals(v.getBuildNumber(), dataVersion.getBuildNumber());
        assertEquals(v.getVersionNumber(), dataVersion.getVersionNumber());
    }
}
