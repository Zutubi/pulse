package com.zutubi.pulse.acceptance;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.zutubi.pulse.acceptance.rpc.RemoteApiClient;
import com.zutubi.pulse.acceptance.rpc.RpcClient;
import com.zutubi.pulse.acceptance.support.Pulse;
import com.zutubi.pulse.acceptance.support.PulsePackage;
import com.zutubi.pulse.acceptance.support.PulseTestFactory;
import com.zutubi.pulse.acceptance.support.SupportUtils;
import com.zutubi.pulse.acceptance.support.jython.JythonPulseTestFactory;
import com.zutubi.pulse.core.plugins.repository.PluginRepository;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.util.NullUnaryProcedure;
import com.zutubi.util.UnaryProcedure;
import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Vector;

import static com.zutubi.pulse.acceptance.rpc.RemoteApiClient.SYMBOLIC_NAME_KEY;
import static com.zutubi.util.Constants.MINUTE;
import static com.zutubi.util.Constants.SECOND;

public class AgentUpgradeAcceptanceTest extends PulseTestCase
{
    private File tmp = null;
    private PulseTestFactory pulseTestFactory = null;
    private Pulse agent;
    private Pulse master;

    protected void setUp() throws Exception
    {
        super.setUp();

        tmp = createTempDirectory();
        pulseTestFactory = new JythonPulseTestFactory();
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
            public void run(Pulse agent)
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
        String conf = Files.toString(file, Charset.defaultCharset());
        FileSystemUtils.createFile(file, conf + "\n");
    }

    private void runAgentUpgrade(UnaryProcedure<Pulse> agentCallback) throws Exception
    {
        prepareMaster();
        prepareAgent();
        agentCallback.run(agent);

        // test
        // a) check that the agent build number is as expected.
        String agentUrl = agent.getServerUrl();
        agentUrl = agentUrl + "/xmlrpc";

        RpcClient agentRpcClient = new RpcClient(agentUrl);
        Integer agentBuild = agentRpcClient.callWithoutToken(RemoteApiClient.API_NAME, "getBuildNumber", agent.getAdminToken());
        assertEquals(200000000, agentBuild.intValue());

        // b) add agent to master.
        Hashtable<String, Object> agentConfig = new Hashtable<String, Object>();
        agentConfig.put(SYMBOLIC_NAME_KEY, "zutubi.agentConfig");
        agentConfig.put("name", "upgrade-agent");
        agentConfig.put("host", "localhost");
        agentConfig.put("port", 7689);

        String masterUrl = master.getServerUrl();
        masterUrl = masterUrl + "/xmlrpc";
        RpcClient masterRpcClient = new RpcClient(masterUrl);
        masterRpcClient.loginAsAdmin();

        masterRpcClient.RemoteApi.insertTemplatedConfig("agents/" + AgentManager.GLOBAL_AGENT_NAME, agentConfig, false);

        Thread.sleep(5 * SECOND);

        masterRpcClient.RemoteApi.waitForAgentToBeIdle("upgrade-agent", 5 * MINUTE);

        agentBuild = agentRpcClient.callWithoutToken(RemoteApiClient.API_NAME, "getBuildNumber", agent.getAdminToken());
        assertFalse(200000000 == agentBuild);

        checkPluginsMatch(masterRpcClient, agentRpcClient);
    }

    private void checkPluginsMatch(RpcClient masterRpcClient, RpcClient agentRpcClient) throws Exception
    {
        Vector<Hashtable<String, Object>> masterPlugins = masterRpcClient.TestApi.getRunningPlugins();
        masterPlugins = new Vector<Hashtable<String, Object>>(Collections2.filter(masterPlugins, new Predicate<Hashtable<String, Object>>()
        {
            public boolean apply(Hashtable<String, Object> plugin)
            {
                return PluginRepository.Scope.CORE.toString().equals(plugin.get("scope")) ||
                        PluginRepository.Scope.SERVER.toString().equals(plugin.get("scope"));
            }
        }));

        Vector<Hashtable<String, Object>> agentPlugins = agentRpcClient.callWithoutToken(RemoteApiClient.API_NAME, "getRunningPlugins", agent.getAdminToken());
        assertEquals(masterPlugins.size(), agentPlugins.size());
        
        for (final Hashtable<String, Object> masterPlugin: masterPlugins)
        {
            assertTrue("Cannot find plugin '" + getId(masterPlugin) + ":" + getVersion(masterPlugin) + "' on agent",
                    Iterables.any(agentPlugins, new Predicate<Hashtable<String, Object>>()
                    {
                        public boolean apply(Hashtable<String, Object> agentPlugin)
                        {
                            return getId(masterPlugin).equals(getId(agentPlugin)) && getVersion(masterPlugin).equals(getVersion(agentPlugin));
                        }
                    }));
        }
    }

    private String getId(Hashtable<String, Object> masterPlugin)
    {
        return (String) masterPlugin.get("id");
    }

    private String getVersion(Hashtable<String, Object> masterPlugin)
    {
        return (String) masterPlugin.get("version");
    }

    private void prepareMaster() throws Exception
    {
        File pulsePackage = AcceptanceTestUtils.getPulsePackage();
        PulsePackage masterPackage = pulseTestFactory.createPackage(pulsePackage);
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
            AcceptanceTestUtils.setPulsePort(7688);
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

        PulsePackage agentPackage = pulseTestFactory.createPackage(oldAgentPackage);
        agent = agentPackage.extractTo(new File(tmp, "agent").getCanonicalPath());
        agent.setUserHome(new File(tmp, "user-home").getCanonicalPath());
        agent.setDataDir(new File(tmp, "agent-data").getCanonicalPath());
        agent.setPort(7689);
        agent.start();
    }
}
