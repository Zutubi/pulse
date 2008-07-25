package com.zutubi.pulse.acceptance;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.JythonPackageFactory;
import com.zutubi.pulse.util.FileSystemUtils;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 *
 */
public class AcceptanceTestSuiteSetupTeardown
{
    private Pulse pulse;
    private File tmp;

    @BeforeSuite
    public void startPulse() throws Exception
    {
        JythonPackageFactory factory= new JythonPackageFactory();
        
        int port = Integer.getInteger("pulse.port");

        String pulsePackageName = System.getProperty("pulse.package");
        File pulsePackage = new File(pulsePackageName);

        System.out.println("Starting pulse: " + pulsePackage.getCanonicalPath());

        tmp = FileSystemUtils.createTempDir();

        System.out.println("using tmp directory: " + tmp.getCanonicalPath());

        PulsePackage pkg = factory.createPackage(pulsePackage);
        pulse = pkg.extractTo(tmp.getCanonicalPath());
        pulse.setPort(port);
        pulse.setUserHome(new File(tmp, "user.home").getCanonicalPath());
        pulse.start(true);

        System.out.println("Pulse started.");
    }

    @AfterSuite
    public void stopPulse()
    {
        System.out.println("Stopping pulse: ");
        pulse.stop();
        if (!FileSystemUtils.rmdir(tmp))
        {
            System.out.println("Failed to clean up. :(");
        }
    }
}
