package com.zutubi.pulse.acceptance.support.jython;

import com.zutubi.pulse.acceptance.AcceptanceTestUtils;
import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.acceptance.support.PulseTestFactory;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

@Test
public class JythonPulseTestFactoryTest extends PulseTestCase
{
    private PulseTestFactory factory;

    private File tmp;
    private File pkgFile;

    @BeforeMethod
    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        factory = new JythonPulseTestFactory();

        pkgFile = AcceptanceTestUtils.getPulsePackage();
    }

    @AfterMethod
    protected void tearDown() throws Exception
    {
        removeDirectory(tmp);
        super.tearDown();
    }

    public void testExtractPackage() throws IOException
    {
        PulsePackage pkg = factory.createPackage(pkgFile);

        Pulse pulse = pkg.extractTo(tmp.getCanonicalPath());
        assertNotNull(pulse);

        // ensure that pulse is extracted as expected.

        String pkgFileName = pkgFile.getName();
        String pkgName;
        if (pkgFileName.endsWith(".tar.gz"))
        {
            pkgName = pkgFileName.substring(0, pkgFileName.length() - 7);
        }
        else
        {
            pkgName = pkgFileName.substring(0, pkgFileName.lastIndexOf('.'));
        }

        File expectedRoot = new File(tmp, pkgName);
        assertTrue(expectedRoot.isDirectory());

        // normalise the paths before we compare them.
        assertEquals(expectedRoot.getCanonicalPath(), new File(pulse.getPulseHome()).getCanonicalPath());

        assertTrue(new File(expectedRoot, "active-version.txt").isFile());
        File versionsDir = new File(expectedRoot, "versions");
        assertTrue(versionsDir.isDirectory());

        File[] installedVersions = versionsDir.listFiles();
        assertEquals(1, installedVersions.length);

        File activeBase = installedVersions[0];
        assertEquals(new File(activeBase, "system/plugins").getCanonicalPath(), new File(pulse.getPluginRoot()).getCanonicalPath());

        assertNull(pulse.getAdminToken());
    }

    public void disabledTestStartAndStopPulse() throws IOException
    {
        PulsePackage pkg = factory.createPackage(pkgFile);
        Pulse pulse = pkg.extractTo(tmp.getCanonicalPath());

        assertFalse(pulse.ping());

        pulse.start(true);
        assertTrue(pulse.ping());

        pulse.stop();
        assertFalse(pulse.ping());
    }

    public void disabledTestSettingAlternateUserHome() throws Exception
    {
        PulsePackage pkg = factory.createPackage(pkgFile);
        Pulse pulse = pkg.extractTo(tmp.getCanonicalPath());

        File alternateUserHome = new File(tmp, "user_home");
        pulse.setUserHome(alternateUserHome.getCanonicalPath());
        pulse.start();

        // by default, the .pulse2 directory will be created in the user home on Pulse startup.
        assertTrue(new File(alternateUserHome, ".pulse2").exists());

        pulse.stop();
    }
}
