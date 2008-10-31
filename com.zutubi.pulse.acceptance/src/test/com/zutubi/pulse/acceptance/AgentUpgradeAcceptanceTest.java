package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.support.JythonPackageFactory;
import com.zutubi.pulse.acceptance.support.PackageFactory;
import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.core.test.PulseTestCase;
import static com.zutubi.util.FileSystemUtils.*;
import com.zutubi.util.Constants;

import java.io.File;
import java.net.URL;
import java.util.Hashtable;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

/**
 *
 *
 */
@Test
public class AgentUpgradeAcceptanceTest extends PulseTestCase
{
    private File tmp = null;

    private PackageFactory packageFactory = null;

    @BeforeMethod
    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDir();
        packageFactory = new JythonPackageFactory();
    }

    @AfterMethod
    protected void tearDown() throws Exception
    {
        packageFactory = null;
        removeDirectory(tmp);
        tmp = null;

        super.tearDown();
    }

    public void testNormalAgentUpgrade() throws Exception
    {
        Pulse agent = null;
        Pulse master = null;

        try
        {
            // get package details.
            File pulsePackage = getPulsePackage();

            // get old agent package that we are upgrading from.
            // a) start with a predefined package, later move to a range of older packages that we can test from.
            File oldAgentPackage = new File(getPulseRoot(), join("com.zutubi.pulse.acceptance", "src", "test", "data", "pulse-agent-2.0.0.zip"));

            // ensure that the two packages exist.

            // unpack and start old agent.
            // agent-port
            // tmp/agent-data
            PulsePackage agentPackage = packageFactory.createPackage(oldAgentPackage);
            agent = agentPackage.extractTo(new File(tmp, "agent").getCanonicalPath());
            agent.setUserHome(new File(tmp, "user-home").getCanonicalPath());
            agent.setDataDir(new File(tmp, "agent-data").getCanonicalPath());
            agent.setPort(7689);
            agent.start();

            // unpack and start master
            // master-port
            // tmp/master-data
            PulsePackage masterPackage = packageFactory.createPackage(pulsePackage);
            master = masterPackage.extractTo(new File(tmp, "master").getCanonicalPath());
            master.setUserHome(new File(tmp, "user-home").getCanonicalPath());
            master.setDataDir(new File(tmp, "master-data").getCanonicalPath());
            master.setPort(7688);
            master.start();

            // work through the master setup.
            // - piggy back of the existing acceptance test.
            try
            {
                System.setProperty("pulse.port", "7688");
                SetupAcceptanceTest setup = new SetupAcceptanceTest();
                setup.setUp();
                // we can not run the usual test since that attempts to specify the data directory.
                setup.checkPostPulseData();
                setup.tearDown();
            }
            catch (RuntimeException e)
            {
                fail("Could not contact Selenium Server");
            }
            catch (Exception e)
            {
                fail(e.getMessage());
            }

            // test
            // a) check that the agent build number is as expected.

            String agentUrl = agent.getServerUrl();
            agentUrl = agentUrl + "/xmlrpc";

            XmlRpcHelper agentXmlRpc = new XmlRpcHelper(new URL(agentUrl));
            Integer agentBuild = agentXmlRpc.callWithoutToken("getBuildNumber", agent.getAdminToken());
            assertEquals(200000000, agentBuild.intValue());

            // b) add agent to master.
            Hashtable<String, Object> agentConfig = new Hashtable<String, Object>();
            agentConfig.put("meta.symbolicName", "zutubi.agentConfig");
            agentConfig.put("name", "upgrade-agent");
            agentConfig.put("host", "localhost");
            agentConfig.put("port", 7689);

            String masterUrl = master.getServerUrl();
            masterUrl = masterUrl + "/xmlrpc";
            XmlRpcHelper masterXmlRpc = new XmlRpcHelper(new URL(masterUrl));
            masterXmlRpc.loginAsAdmin();

            masterXmlRpc.insertTemplatedConfig("agents/global agent template", agentConfig, false);

            //RemoteApi.getAgentStatus(master.getAdminToken(), 'upgrade-agent')
            long endTime = System.currentTimeMillis() + 5 * Constants.MINUTE;
            Thread.sleep(5 * Constants.SECOND);

            while (System.currentTimeMillis() < endTime)
            {
                String status = (String) masterXmlRpc.getAgentStatus("upgrade-agent");
                if ("idle".equals(status))
                {
                    // success.
                    agentBuild = agentXmlRpc.callWithoutToken("getBuildNumber", agent.getAdminToken());
                    assertFalse(200000000 == agentBuild.intValue());
                    return;
                }
            }
            fail("Failed to upgrade agent.");
        }
        finally
        {
            shutdown(agent);
            shutdown(master);
        }
    }

    private void shutdown(Pulse instance)
    {
        if (instance != null)
        {
            try
            {
                instance.stop();
            }
            catch (Exception e)
            {
                System.out.println("Failed to shutdown pulse instance: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
