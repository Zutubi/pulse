package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.rpc.RpcClient;
import com.zutubi.pulse.acceptance.utils.ConfigurationHelper;
import com.zutubi.pulse.acceptance.utils.ProjectConfigurations;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.time.TimeStamps;
import junit.framework.AssertionFailedError;

import java.util.Hashtable;
import java.util.Vector;

import static com.zutubi.pulse.acceptance.AcceptanceTestUtils.getPulseUrl;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.AGENTS_SCOPE;
import static com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry.PROJECTS_SCOPE;
import static com.zutubi.tove.type.record.PathUtils.getPath;

/**
 * The base class for all acceptance level tests.  It provides some useful
 * support for debugging acceptance tests.  In particular, screenshots are taken
 * when tests that use a browser fail, and the execution of a test is sent to
 * standard out so that it can be picked up in the logs.
 */
public abstract class AcceptanceTestBase extends PulseTestCase
{
    /**
     * Shared agent used for simple single-agent builds.  Makes it easier to
     * run these tests in development environments (just manually run one
     * agent on port 8890).
     */
    public static final String AGENT_NAME = "localhost";

    /**
     * We reuse the same configuration helper instance as it is expensive to initialise.
     */
    protected static final ConfigurationHelper CONFIGURATION_HELPER = new ConfigurationHelper();
    
    protected RpcClient rpcClient;
    protected ProjectConfigurations projectConfigurations;
    protected String baseUrl;

    protected Urls urls;
    protected String random;

    private SeleniumBrowser browser;
    private SeleniumBrowserFactory browserFactory;

    protected void setUp() throws Exception
    {
        super.setUp();

        baseUrl = getPulseUrl();
        rpcClient = new RpcClient();
        
        CONFIGURATION_HELPER.setRemoteApi(rpcClient.RemoteApi);
        CONFIGURATION_HELPER.init();

        projectConfigurations = new ProjectConfigurations(CONFIGURATION_HELPER);
        random = randomName();

        browserFactory = new SingleSeleniumBrowserFactory();
        browserFactory.cleanup();

        urls = new Urls(baseUrl);
    }

    protected void tearDown() throws Exception
    {
        try
        {
            browserFactory.cleanup();
            if (rpcClient.isLoggedIn())
            {
                rpcClient.logout();
            }
        }
        catch (Exception e)
        {
            // Don't allow these errors to mask more useful ones from the tests.
            e.printStackTrace();
        }

        super.tearDown();
    }

    protected String randomName()
    {
        return getName() + "-" + RandomUtils.randomString(10);
    }

    public SeleniumBrowser getBrowser()
    {
        if (browser == null)
        {
            browser = browserFactory.newBrowser();
        }
        return browser;
    }

    /**
     * Runs the bare test sequence.
     *
     * @throws Throwable if any exception is thrown
     */
    @Override
    public void runBare() throws Throwable
    {
        // leave a trace in the output so that we can see the progress of the tests.
        System.out.print(getClass().getName() + ":" + getName() + " ");
        System.out.flush(); // flush here since we do not write a full line above.

        long startTime = 0;

        setUp();
        try
        {
            startTime = System.currentTimeMillis();
            
            runTest();

            System.out.print("[success]");
        }
        catch (Throwable t)
        {
            if (t instanceof AssertionFailedError)
            {
                System.out.print("[failed]");
            }
            else
            {
                System.out.print("[error]");
            }

            throw t;
        }
        finally
        {
            System.out.println(" " + TimeStamps.getPrettyElapsed(System.currentTimeMillis() - startTime));
            tearDown();
        }
    }

    protected void removeNonMasterAgents() throws Exception
    {
        // Ensure only the master agent is defined.
        Vector<String> allAgents = rpcClient.RemoteApi.getAllAgentNames();
        for (String agent: allAgents)
        {
            if (!agent.equals(AgentManager.MASTER_AGENT_NAME))
            {
                rpcClient.RemoteApi.deleteConfig(PathUtils.getPath(MasterConfigurationRegistry.AGENTS_SCOPE, agent));
            }
        }
    }

    protected void assignStageToAgent(String projectName, String stageName, String agentName) throws Exception
    {
        String stagePath = getPath(PROJECTS_SCOPE, projectName, "stages", stageName);
        Hashtable<String, Object> defaultStage = rpcClient.RemoteApi.getConfig(stagePath);
        defaultStage.put("agent", getPath(AGENTS_SCOPE, agentName));
        rpcClient.RemoteApi.saveConfig(stagePath, defaultStage, false);
    }
}
