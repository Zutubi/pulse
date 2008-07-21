package com.zutubi.pulse.acceptance.support;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
public class JythonPackageFactoryTest extends PulseTestCase
{
    private PackageFactory factory;

    private File tmp;
    private File packagesDir;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        factory = new JythonPackageFactory();

        String packageDirectoryName = StringUtils.join(File.pathSeparator, "testing-packages");
        packagesDir = new File(getPulseRoot(), packageDirectoryName);
    }

    protected void tearDown() throws Exception
    {
        factory.close();

        try
        {
            removeDirectory(tmp);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        super.tearDown();
    }

    public void disabledTestExtractPackage() throws IOException
    {
        PulsePackage pkg = factory.createPackage(new File(packagesDir, "pulse-2.0.0.zip"));

        Pulse pulse = pkg.extractTo(tmp.getCanonicalPath());
        assertNotNull(pulse);

        // ensure that pulse is extracted as expected.
        File expectedRoot = new File(tmp, "pulse-2.0.0");
        assertTrue(expectedRoot.isDirectory());

        // normalise the paths before we compare them.
        assertEquals(expectedRoot.getCanonicalPath(), new File(pulse.getRoot()).getCanonicalPath());

        File expectedPluginRoot = new File(expectedRoot, "versions/0200000000/system/plugins");
        assertEquals(expectedPluginRoot.getCanonicalPath(), new File(pulse.getPluginRoot()).getCanonicalPath());
    }

    public void disabledTestStartAndStopPulse() throws IOException
    {
        PulsePackage pkg = factory.createPackage(new File(packagesDir, "pulse-2.0.0.zip"));
        Pulse pulse = pkg.extractTo(tmp.getCanonicalPath());

        assertFalse(pulse.ping());

        pulse.start(true);
        assertTrue(pulse.ping());

        pulse.stop();
        assertFalse(pulse.ping());
    }

    public void disabledTestAddingJavaOpts() throws Exception
    {
        PulsePackage pkg = factory.createPackage(new File(packagesDir, "pulse-2.0.0.zip"));
        Pulse pulse = pkg.extractTo(tmp.getCanonicalPath());

        File alternateUserHome = new File(tmp, "user_home");
        pulse.setUserHome(alternateUserHome.getCanonicalPath());
        pulse.start();

        assertTrue(new File(alternateUserHome, ".pulse2").exists());

        pulse.stop();
    }
}
