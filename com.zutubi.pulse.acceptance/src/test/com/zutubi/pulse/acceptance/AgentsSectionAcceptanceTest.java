package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.components.pulse.agent.AgentInfo;
import com.zutubi.pulse.acceptance.components.pulse.agent.AgentSummaryTable;
import com.zutubi.pulse.acceptance.forms.admin.AgentForm;
import com.zutubi.pulse.acceptance.pages.admin.AgentHierarchyPage;
import com.zutubi.pulse.acceptance.pages.agents.AgentStatisticsPage;
import com.zutubi.pulse.acceptance.pages.agents.AgentsPage;
import com.zutubi.pulse.acceptance.rpc.RemoteApiClient;
import com.zutubi.pulse.acceptance.utils.WaitProject;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.agent.AgentManager;
import static com.zutubi.pulse.master.agent.AgentStatus.*;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import static com.zutubi.pulse.master.tove.config.agent.AgentConfigurationActions.*;
import com.zutubi.util.Condition;
import com.zutubi.util.io.FileSystemUtils;
import static java.util.Arrays.asList;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Acceptance test for basic agents section functionality.
 */
public class AgentsSectionAcceptanceTest extends AcceptanceTestBase
{
    private static final String LOCAL_AGENT = "local-agent";
    private static final String HOST_LOCALHOST = "localhost";
    private static final String STATUS_DISABLE_ON_IDLE = "disable on idle";

    private File tempDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient.loginAsAdmin();
        removeNonMasterAgents();
        tempDir = FileSystemUtils.createTempDir(getName());
    }

    @Override
    protected void tearDown() throws Exception
    {
        removeNonMasterAgents();
        rpcClient.cancelIncompleteBuilds();
        rpcClient.logout();

        removeDirectory(tempDir);

        super.tearDown();
    }

    public void testOfflineAgent() throws Exception
    {
        final String AGENT = "offline-agent";

        CONFIGURATION_HELPER.insertAgent(new AgentConfiguration(AGENT, HOST_LOCALHOST, 8899));

        getBrowser().loginAsAdmin();
        final AgentsPage agentsPage = getBrowser().openAndWaitFor(AgentsPage.class);
        agentsPage.refreshUntilStatus(AGENT, OFFLINE.getPrettyString());

        long agentId = rpcClient.TestApi.getAgentId(AGENT);
        final List<String> actions = agentsPage.getAgentSummaryTable().getActions(agentId);
        Collections.sort(actions);
        assertEquals(asList(ACTION_CLEAN, ACTION_DISABLE, ACTION_PING), actions);
    }

    public void testDisableEnable() throws Exception
    {
        CONFIGURATION_HELPER.insertAgent(new AgentConfiguration(LOCAL_AGENT, HOST_LOCALHOST, 8890));
        rpcClient.RemoteApi.waitForAgentToBeIdle(LOCAL_AGENT);
        long agentId = rpcClient.TestApi.getAgentId(LOCAL_AGENT);
        
        getBrowser().loginAsAdmin();
        AgentsPage agentsPage = getBrowser().openAndWaitFor(AgentsPage.class);
        final AgentSummaryTable summaryTable = agentsPage.getAgentSummaryTable();
        assertTrue(summaryTable.areActionsAvailable(agentId, ACTION_DISABLE));
        summaryTable.clickAction(agentId, ACTION_DISABLE);

        agentsPage.refreshUntilStatus(LOCAL_AGENT, DISABLED.getPrettyString());
        
        assertTrue(summaryTable.areActionsAvailable(agentId, ACTION_ENABLE));
        summaryTable.clickAction(agentId, ACTION_ENABLE);
        
        agentsPage.refreshUntilStatus(LOCAL_AGENT, IDLE.getPrettyString());
    }

    public void testDisableOnIdle() throws Exception
    {
        CONFIGURATION_HELPER.insertAgent(new AgentConfiguration(LOCAL_AGENT, HOST_LOCALHOST, 8890));
        long agentId = rpcClient.TestApi.getAgentId(LOCAL_AGENT);

        WaitProject project = projectConfigurations.createWaitAntProject(randomName(), tempDir, false);
        project.getDefaultStage().setAgent(CONFIGURATION_HELPER.getAgentReference(LOCAL_AGENT));
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);

        rpcClient.RemoteApi.triggerBuild(project.getName());
        rpcClient.RemoteApi.waitForBuildInProgress(project.getName(), 1);

        getBrowser().loginAsAdmin();
        AgentsPage agentsPage = getBrowser().openAndWaitFor(AgentsPage.class);
        AgentSummaryTable summaryTable = agentsPage.getAgentSummaryTable();
        summaryTable.clickAction(agentId, ACTION_DISABLE);

        agentsPage.refreshUntilStatus(LOCAL_AGENT, STATUS_DISABLE_ON_IDLE);

        assertTrue(summaryTable.areActionsAvailable(agentId, ACTION_ENABLE, ACTION_DISABLE_NOW));
        summaryTable.clickAction(agentId, ACTION_ENABLE);
        agentsPage.openAndWaitFor();
        summaryTable.clickAction(agentId, ACTION_PING);

        agentsPage.refreshUntilStatus(LOCAL_AGENT, BUILDING.getPrettyString(), RECIPE_ASSIGNED.getPrettyString(), RECIPE_DISPATCHED.getPrettyString());

        assertTrue(rpcClient.RemoteApi.getAgentEnableState(LOCAL_AGENT).isEnabled());

        summaryTable.clickAction(agentId, ACTION_DISABLE);

        agentsPage.refreshUntilStatus(LOCAL_AGENT, STATUS_DISABLE_ON_IDLE);

        assertTrue(rpcClient.RemoteApi.getAgentEnableState(LOCAL_AGENT).isDisabling());

        project.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), 1);
        rpcClient.RemoteApi.waitForAgentStatus(LOCAL_AGENT, DISABLED, RemoteApiClient.BUILD_TIMEOUT);
    }

    public void testConcurrentBuildsSameHost() throws Exception
    {
        String agent1 = LOCAL_AGENT + "-1";
        String agent2 = LOCAL_AGENT + "-2";

        CONFIGURATION_HELPER.insertAgent( new AgentConfiguration(agent1, HOST_LOCALHOST, 8890));
        CONFIGURATION_HELPER.insertAgent(new AgentConfiguration(agent2, HOST_LOCALHOST, 8890));

        String random = randomName();
        WaitProject project1 = projectConfigurations.createWaitAntProject(random + "-1", tempDir, false);
        WaitProject project2 = projectConfigurations.createWaitAntProject(random + "-2", tempDir, false);
        project1.getDefaultStage().setAgent(CONFIGURATION_HELPER.getAgentReference(agent1));
        project2.getDefaultStage().setAgent(CONFIGURATION_HELPER.getAgentReference(agent2));
        CONFIGURATION_HELPER.insertProject(project1.getConfig(), false);
        CONFIGURATION_HELPER.insertProject(project2.getConfig(), false);
        rpcClient.RemoteApi.triggerBuild(project1.getName());
        rpcClient.RemoteApi.waitForBuildInProgress(project1.getName(), 1);
        rpcClient.RemoteApi.triggerBuild(project2.getName());
        rpcClient.RemoteApi.waitForBuildInProgress(project2.getName(), 1);

        getBrowser().loginAsAdmin();
        AgentsPage agentsPage = getBrowser().openAndWaitFor(AgentsPage.class);
        final AgentSummaryTable summaryTable = agentsPage.getAgentSummaryTable();
        assertBuildingStatus(summaryTable.getStatus(agent1));
        assertBuildingStatus(summaryTable.getStatus(agent2));

        project2.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(project2.getName(), 1);

        agentsPage.openAndWaitFor();
        assertBuildingStatus(summaryTable.getStatus(agent1));
        rpcClient.RemoteApi.waitForAgentToBeIdle(agent2);

        project1.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(project1.getName(), 1);
        rpcClient.RemoteApi.waitForAgentToBeIdle(agent1);
    }

    public void testHostOptionProvider() throws Exception
    {
        final String HOST_1 = "host1";
        final String HOST_2 = "host2";

        String random = randomName();
        String agent1 = random + "-1";
        String agent2 = random + "-2";
        String agent3 = random + "-3";

        CONFIGURATION_HELPER.insertAgent( new AgentConfiguration(agent1, HOST_1, 8890));
        CONFIGURATION_HELPER.insertAgent( new AgentConfiguration(agent2, HOST_2, 8890));
        CONFIGURATION_HELPER.insertAgent( new AgentConfiguration(agent3, HOST_1, 8890));

        getBrowser().loginAsAdmin();

        AgentHierarchyPage globalAgentPage = getBrowser().openAndWaitFor(AgentHierarchyPage.class, AgentManager.GLOBAL_AGENT_NAME, true);
        globalAgentPage.clickAdd();

        AgentForm agentForm = getBrowser().createForm(AgentForm.class, true);
        agentForm.waitFor();
        assertEquals(asList("", HOST_1, HOST_2), agentForm.getComboBoxOptions("host"));
    }
    
    public void testAgentsExecutingBuild() throws Exception
    {
        getBrowser().loginAsAdmin();
        final AgentsPage agentsPage = getBrowser().openAndWaitFor(AgentsPage.class);
        final AgentSummaryTable summaryTable = agentsPage.getAgentSummaryTable();
        assertNull(summaryTable.getAgent(AgentManager.MASTER_AGENT_NAME).executingOwner);

        WaitProject project = projectConfigurations.createWaitAntProject(random, tempDir, false);
        project.getDefaultStage().setAgent(CONFIGURATION_HELPER.getAgentReference(AgentManager.MASTER_AGENT_NAME));
        CONFIGURATION_HELPER.insertProject(project.getConfig(), false);
        rpcClient.RemoteApi.triggerBuild(project.getName());
        rpcClient.RemoteApi.waitForBuildInProgress(project.getName(), 1);

        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                agentsPage.openAndWaitFor();
                return summaryTable.getAgent(AgentManager.MASTER_AGENT_NAME).executingOwner != null;
            }
        }, SeleniumBrowser.REFRESH_TIMEOUT, "executing build to appear on master agent");

        AgentInfo info = summaryTable.getAgent(AgentManager.MASTER_AGENT_NAME);
        assertEquals(project.getName(), info.executingOwner);
        assertEquals("1", info.executingNumber);
        assertEquals("default", info.executingStage);

        project.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), 1);
    }

    public void testStatistics() throws Exception
    {
        getBrowser().loginAsAdmin();
        AgentStatisticsPage statisticsPage = getBrowser().openAndWaitFor(AgentStatisticsPage.class, AgentManager.MASTER_AGENT_NAME);
        assertTrue(statisticsPage.isRecipeStatisticsPresent());
        assertTrue(statisticsPage.isUsageStatisticsPresent());
        assertTrue(statisticsPage.isUsageChartPresent());
    }

    private void assertBuildingStatus(String status)
    {
        assertTrue("Status '" + status + "' is not a building status",
                status.equals(BUILDING.getPrettyString()) || status.equals(RECIPE_ASSIGNED.getPrettyString()) || status.equals(RECIPE_DISPATCHED.getPrettyString()));
    }
}
