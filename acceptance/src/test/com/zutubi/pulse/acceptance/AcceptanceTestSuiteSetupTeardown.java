package com.zutubi.pulse.acceptance;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.JythonPackageFactory;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.util.TextUtils;

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
    private Pulse agent;

    public AcceptanceTestSuiteSetupTeardown(junit.framework.Test test)
    {
        super(test);
    }

    @BeforeSuite
    public void setUp() throws Exception
    {
        JythonPackageFactory factory = new JythonPackageFactory();

        int port = Integer.getInteger("pulse.port");

        File pulsePackage = PulseTestCase.getPulsePackage();

        File dir = new File(System.getProperty("work.dir"));

        PulsePackage pkg = factory.createPackage(pulsePackage);
        pulse = pkg.extractTo(new File(dir, "master").getCanonicalPath());
        pulse.setVerbose(true);
        pulse.setPort(port);
        pulse.setUserHome(new File(dir, "user.home").getCanonicalPath());
        pulse.start();

        // start up an agent as well.  port 8890
        File agentPackage = getAgentPackage();
        PulsePackage agentPkg = factory.createPackage(agentPackage);
        agent = agentPkg.extractTo(new File(dir, "agent").getCanonicalPath());
        agent.setPort(8890);
        agent.setUserHome(new File(dir, "user.home").getCanonicalPath());
        agent.start();
    }

    @AfterSuite
    public void tearDown() throws IOException
    {
        shutdown(pulse);
        pulse = null;

        shutdown(agent);
        agent = null;
    }

    public static File getAgentPackage()
    {
        String agentPackage = System.getProperty("agent.package");
        if (!TextUtils.stringSet(agentPackage))
        {
            return null;
        }
        File pkg = new File(agentPackage);
        if (!pkg.isFile())
        {
            throw new IllegalStateException("Unexpected invalid agent.package: " + agentPackage + " does not reference a file.");
        }
        return pkg;
    }

    protected void shutdown(Pulse pulse)
    {
        try
        {
            if (pulse != null)
            {
                pulse.stop();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
