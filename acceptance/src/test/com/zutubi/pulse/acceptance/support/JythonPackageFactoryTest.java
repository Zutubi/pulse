package com.zutubi.pulse.acceptance.support;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.StringUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
@Test
public class JythonPackageFactoryTest extends PulseTestCase
{
    private PackageFactory factory;

    private File tmp;
    private File pkgFile;

    @BeforeMethod
    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        factory = new JythonPackageFactory();

        pkgFile = getPulsePackage();
/*
        if (pkgFile == null)
        {
            String packageDirectoryName = StringUtils.join(File.pathSeparator, "test-packages");
            File packagesDir = new File(getPulseRoot(), packageDirectoryName);

            pkgFile = new File(packagesDir, "pulse-2.0.9.zip");
            assertTrue(pkgFile.isFile());
        }
*/
    }

    @AfterMethod
    protected void tearDown() throws Exception
    {
        factory.close();
        factory = null;

        removeDirectory(tmp);

        super.tearDown();
    }

    public void testExtractPackage() throws IOException
    {
        PulsePackage pkg = factory.createPackage(pkgFile);

        Pulse pulse = pkg.extractTo(tmp.getCanonicalPath());
        assertNotNull(pulse);

        // ensure that pulse is extracted as expected.
        File expectedRoot = new File(tmp, "pulse-2.0.9");
        assertTrue(expectedRoot.isDirectory());

        // normalise the paths before we compare them.
        assertEquals(expectedRoot.getCanonicalPath(), new File(pulse.getPulseHome()).getCanonicalPath());

        File expectedPluginRoot = new File(expectedRoot, "versions/0200009000/system/plugins");
        assertEquals(expectedPluginRoot.getCanonicalPath(), new File(pulse.getPluginRoot()).getCanonicalPath());
    }

    public void testStartAndStopPulse() throws IOException
    {
        PulsePackage pkg = factory.createPackage(pkgFile);
        Pulse pulse = pkg.extractTo(tmp.getCanonicalPath());

        assertFalse(pulse.ping());

        pulse.start(true);
        assertTrue(pulse.ping());

        pulse.stop();
        assertFalse(pulse.ping());
    }

    public void testAddingJavaOpts() throws Exception
    {
        PulsePackage pkg = factory.createPackage(pkgFile);
        Pulse pulse = pkg.extractTo(tmp.getCanonicalPath());

        File alternateUserHome = new File(tmp, "user_home");
        pulse.setUserHome(alternateUserHome.getCanonicalPath());
        pulse.start();

        assertTrue(new File(alternateUserHome, ".pulse2").exists());

        pulse.stop();
    }
}
