package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.command.ShutdownCommand;
import com.zutubi.pulse.command.StartCommand;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
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
//        super.setUp();

        tmpDir = FileSystemUtils.createTempDirectory("UpgradeAccepanceTest", this.getName());
    }

    protected void tearDown() throws Exception
    {
        if (!FileSystemUtils.removeDirectory(tmpDir))
        {
            throw new RuntimeException();
        }

//        super.tearDown();
    }

    public void testUpgradeFromVersionOnePointOne() throws IOException, InterruptedException, SAXException
    {
        System.setProperty("bootstrap", "com/zutubi/pulse/bootstrap/ideaBootstrapContext.xml");

        // extract zip file.
        InputStream is = null;
        try
        {
            is = getClass().getResourceAsStream("data/pulse-1.1.0-data.zip");
            assertNotNull(is);
            FileSystemUtils.extractZip(new ZipInputStream(is), tmpDir);
        }
        finally
        {
            IOUtils.close(is);
        }

        // start pulse using the extracted data directory.
        StartCommand start = new StartCommand();
        start.setPort(8990);
        start.setData(tmpDir.getAbsolutePath());
        assertEquals(0, start.execute());

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
        shutdown.setForce(true);
        shutdown.setPort(8990);
        shutdown.execute();

        // allow time for the shutdown to complete.
        Thread.sleep(2000);
    }

}
