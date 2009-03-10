package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.support.jython.JythonPackageFactory;
import com.zutubi.pulse.acceptance.support.PackageFactory;
import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.acceptance.support.SupportUtils;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.Constants;
import com.zutubi.util.FileSystemUtils;
import static com.zutubi.util.FileSystemUtils.createTempDir;
import com.zutubi.util.NullUnaryProcedure;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

public class AgentUpgradeAcceptanceTest extends PulseTestCase
{
    private File tmp = null;
    private PackageFactory packageFactory = null;
    private Pulse agent;
    private Pulse master;

    protected void setUp() throws Exception
    {
        super.setUp();

        System.setProperty("pulse.package", "pulse-2.1.0-dev.zip");
        System.setProperty("agent.package", "pulse-agent-2.1.0-dev.zip");

        tmp = createTempDir();
        packageFactory = new JythonPackageFactory();
    }

    protected void tearDown() throws Exception
    {
        SupportUtils.shutdown(agent);
        SupportUtils.shutdown(master);

        removeDirectory(tmp);
        super.tearDown();
    }

    public void testNormalAgentUpgrade() throws Exception
    {
        runAgentUpgrade(new NullUnaryProcedure<Pulse>());
    }

    public void testUserEditedFiles() throws Exception
    {
        runAgentUpgrade(new UnaryProcedure<Pulse>()
        {
            public void process(Pulse agent)
            {
                try
                {
                    File bin = new File(agent.getPulseHome(), "bin");

                    // Users may edit wrapper.conf to tune settings
                    editFile(new File(bin, "wrapper.conf"));
                    // Another common file to edit is init.sh
                    editFile(new File(bin, "init.sh"));
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void editFile(File file) throws IOException
    {
        assertTrue(file.isFile());
        String conf = IOUtils.fileToString(file);
        FileSystemUtils.createFile(file, conf + "\n");
    }

    private void runAgentUpgrade(UnaryProcedure<Pulse> agentCallback) throws Exception
    {
        prepareMaster();
        prepareAgent();
        agentCallback.process(agent);

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

        long endTime = System.currentTimeMillis() + 5 * Constants.MINUTE;
        Thread.sleep(5 * Constants.SECOND);

        while (System.currentTimeMillis() < endTime)
        {
            String status = (String) masterXmlRpc.getAgentStatus("upgrade-agent");
            if ("idle".equals(status))
            {
                // success.
                agentBuild = agentXmlRpc.callWithoutToken("getBuildNumber", agent.getAdminToken());
                assertFalse(200000000 == agentBuild);
                return;
            }
        }
        fail("Failed to upgrade agent.");
    }

    private void prepareMaster() throws Exception
    {
        File pulsePackage = AcceptanceTestUtils.getPulsePackage();
        PulsePackage masterPackage = packageFactory.createPackage(pulsePackage);
        master = masterPackage.extractTo(new File(tmp, "master").getCanonicalPath());
        master.setUserHome(new File(tmp, "user-home").getCanonicalPath());
        master.setDataDir(new File(tmp, "master-data").getCanonicalPath());
        master.setPort(7688);
        master.start();

        setupMaster();
    }

    private void setupMaster()
    {
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
            fail(e.getMessage());
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    private void prepareAgent() throws Exception
    {
        // get old agent package that we are upgrading from.
        // a) start with a predefined package, later move to a range of older packages that we can test from.
        File oldAgentPackage = copyInputToDirectory("pulse-agent-2.0.0", "zip", tmp);

        PulsePackage agentPackage = packageFactory.createPackage(oldAgentPackage);
        agent = agentPackage.extractTo(new File(tmp, "agent").getCanonicalPath());
        agent.setUserHome(new File(tmp, "user-home").getCanonicalPath());
        agent.setDataDir(new File(tmp, "agent-data").getCanonicalPath());
        agent.setPort(7689);
        agent.start();
    }
}
