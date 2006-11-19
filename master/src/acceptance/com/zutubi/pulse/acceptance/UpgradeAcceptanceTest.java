package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.command.BootContext;
import com.zutubi.pulse.command.ShutdownCommand;
import com.zutubi.pulse.command.StartCommand;
import com.zutubi.pulse.test.TestUtils;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * <class-comment/>
 */
public class UpgradeAcceptanceTest extends BaseAcceptanceTestCase
{
    private File tmpDir = null;

    protected void setUp() throws Exception
    {
        tmpDir = FileSystemUtils.createTempDir("UAT", "");
    }

    protected void tearDown() throws Exception
    {
        if (!FileSystemUtils.rmdir(tmpDir))
        {
            throw new RuntimeException("Failed to remove the temporary directory: " + tmpDir.getAbsolutePath());
        }
    }

    public void testUpgradeFromVersionOnePointOne() throws Exception
    {
        System.setProperty("bootstrap", "com/zutubi/pulse/bootstrap/ideaBootstrapContext.xml");

        // extract zip file.
        InputStream is = null;
        try
        {
            File root = TestUtils.getPulseRoot();
            File data = new File(root, FileSystemUtils.composeFilename("master", "src", "acceptance", "data", "pulse-1.1.0-data.zip"));
            is = new FileInputStream(data);
            
            assertNotNull(is);
            FileSystemUtils.extractZip(new ZipInputStream(is), tmpDir);
        }
        finally
        {
            IOUtils.close(is);
        }

        // start pulse using the extracted data directory.
        StartCommand start = new StartCommand();
        assertEquals(0, start.execute(getBootContext("start", "-p", "8990", "-d", tmpDir.getAbsolutePath())));

        // now we need to go to the Web UI and wait.

        getTestContext().setBaseUrl("http://localhost:8990");
        beginAt("/");

        // check that we have received the upgrade preview, and that the data is as expected.
        assertTextPresent("Upgrade Preview");
        assertTextPresent("0101000000");
        assertTextPresent("1.1.0");

        // we expect at least 11 upgrade tasks to be offered.
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
