package com.zutubi.pulse.acceptance.support.jython;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.getAgentPackage;
import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.getPulsePackage;
import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 */
public class JythonPackageFactoryTest extends PulseTestCase
{
    private JythonPackageFactory factory;

    private File tmp;
    private File serverPkg;
    private File agentPkg;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = FileSystemUtils.createTempDir();

        factory = new JythonPackageFactory();

        System.setProperty("pulse.package", "pulse-2.1.0-dev.zip");
        System.setProperty("agent.package", "pulse-agent-2.1.0-dev.zip");

        serverPkg = getPulsePackage();
        agentPkg = getAgentPackage();
    }

    protected void tearDown() throws Exception
    {
        factory.close();

        removeDirectory(tmp);

        super.tearDown();
    }

    public void testExtractPackage() throws IOException
    {
        PulsePackage pkg = factory.createPackage(serverPkg);

        Pulse pulse = pkg.extractTo(tmp.getCanonicalPath());
        assertNotNull(pulse);

        // ensure that pulse is extracted as expected.

        String pkgFileName = serverPkg.getName();
        String pkgName;
        if (pkgFileName.endsWith(".tar.gz"))
        {
            pkgName = pkgFileName.substring(0, pkgFileName.length() - 7);
        }
        else
        {
            // we assume a single file extension archive.
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

    public void testStartAndStopPulseServer() throws IOException
    {
        assertStartAndStop(serverPkg, 1111);
    }

    public void testStartAndStopPulseAgent() throws IOException
    {
        assertStartAndStop(agentPkg, 1112);
    }

    private void assertStartAndStop(File pkg, int port) throws IOException
    {
        PulsePackage pulsePackage = factory.createPackage(pkg);
        Pulse pulse = pulsePackage.extractTo(tmp.getCanonicalPath());
        pulse.setPort(port);
        assertFalse(pulse.ping());

        // once pulse has started, we do not wait it waiting around, so regardless
        // of what happens, we make sure we call stop.  Lets just hope it works.
        try
        {
            pulse.start(true);
            assertTrue(pulse.ping());
        }
        finally
        {
            pulse.stop();
        }
        assertFalse(pulse.ping());
    }

    public void testSettingAlternateUserHome() throws Exception
    {
        PulsePackage pkg = factory.createPackage(serverPkg);
        Pulse pulse = pkg.extractTo(tmp.getCanonicalPath());

        File alternateUserHome = new File(tmp, "user_home");
        pulse.setUserHome(alternateUserHome.getCanonicalPath());
        pulse.setPort(1113);
        
        try
        {
            pulse.start();
        }
        finally
        {
            pulse.stop();
        }

        // by default, the .pulse2 directory will be created in the user home on Pulse startup.
        assertTrue(new File(alternateUserHome, ".pulse2").exists());
    }
}
