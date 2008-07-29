package com.zutubi.pulse.acceptance;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.JythonPackageFactory;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.test.PulseTestCase;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.extensions.TestSetup;

/**
 *
 *
 */
public class AcceptanceTestSuiteSetupTeardown extends TestSetup
{
    private Pulse pulse;
    private File tmp;

    public AcceptanceTestSuiteSetupTeardown(junit.framework.Test test)
    {
        super(test);
    }

    @BeforeSuite
    public void setUp() throws Exception
    {
        JythonPackageFactory factory= new JythonPackageFactory();
        
        int port = Integer.getInteger("pulse.port");

        File pulsePackage = PulseTestCase.getPulsePackage();

        tmp = FileSystemUtils.createTempDir();

        PulsePackage pkg = factory.createPackage(pulsePackage);
        pulse = pkg.extractTo(tmp.getCanonicalPath());
        pulse.setPort(port);
        pulse.setUserHome(new File(tmp, "user.home").getCanonicalPath());
        pulse.start(true);
    }

    @AfterSuite
    public void tearDown() throws IOException
    {
        pulse.stop();
        pulse = null;
        
        PulseTestCase.removeDirectory(tmp);
    }
}
