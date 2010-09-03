package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.acceptance.support.SupportUtils;
import com.zutubi.pulse.acceptance.support.jython.JythonPulseTestFactory;
import junit.extensions.TestSetup;

import java.io.File;
import java.io.IOException;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.getAgentPort;
import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.getPulsePort;

public class StartPulseTestSetup extends TestSetup
{
    public static final String WORK_DIR_MASTER = "master";
    public static final String WORK_DIR_AGENT = "agent";

    private Pulse pulse;
    private Pulse agent;

    public StartPulseTestSetup(junit.framework.Test test)
    {
        super(test);
    }

    public void setUp() throws Exception
    {
        JythonPulseTestFactory factory = new JythonPulseTestFactory();

        int pulsePort = getPulsePort();
        int agentPort = getAgentPort();

        File dir = AcceptanceTestUtils.getWorkingDirectory();

        File userHome = new File(dir, "user.home");
        
        File pulsePackage = AcceptanceTestUtils.getPulsePackage();
        PulsePackage pkg = factory.createPackage(pulsePackage);
        pulse = pkg.extractTo(new File(dir, WORK_DIR_MASTER).getCanonicalPath());
        pulse.setPort(pulsePort);
        pulse.setUserHome(userHome.getCanonicalPath());
        pulse.start();

        // start up an agent as well.  port 8890
        File agentPackage = AcceptanceTestUtils.getAgentPackage();
        PulsePackage agentPkg = factory.createPackage(agentPackage);
        agent = agentPkg.extractTo(new File(dir, WORK_DIR_AGENT).getCanonicalPath());
        agent.setPort(agentPort);
        agent.setUserHome(userHome.getCanonicalPath());
        agent.start();
    }

    public void tearDown() throws IOException
    {
        SupportUtils.shutdown(pulse);
        pulse = null;

        SupportUtils.shutdown(agent);
        agent = null;
    }
}
