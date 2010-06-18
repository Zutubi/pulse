package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.acceptance.forms.admin.AgentForm;
import com.zutubi.pulse.acceptance.pages.admin.AgentHierarchyPage;
import com.zutubi.pulse.acceptance.pages.agents.AgentStatisticsPage;
import com.zutubi.pulse.acceptance.pages.agents.AgentStatusPage;
import com.zutubi.pulse.acceptance.pages.agents.AgentsPage;
import com.zutubi.pulse.master.agent.AgentManager;
import static com.zutubi.pulse.master.agent.AgentStatus.*;
import com.zutubi.pulse.master.tove.config.MasterConfigurationRegistry;
import static com.zutubi.pulse.master.tove.config.agent.AgentConfigurationActions.*;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.FileSystemUtils;

import java.io.File;
import static java.util.Arrays.asList;
import java.util.Vector;

/**
 * Acceptance test for basic agents section functionality.
 */
public class AgentsSectionAcceptanceTest extends SeleniumTestBase
{
    private static final String LOCAL_AGENT = "local-agent";
    private static final String HOST_LOCALHOST = "localhost";
    private static final String STATUS_DISABLE_ON_IDLE = "disable on idle";

    private ConfigurationHelper configurationHelper;
    private ProjectConfigurations projects;
    private File tempDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        xmlRpcHelper.loginAsAdmin();
        removeNonMasterAgents();

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(xmlRpcHelper);

        projects = new ProjectConfigurations(configurationHelper);

        tempDir = FileSystemUtils.createTempDir(getName());
    }

    @Override
    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tempDir);

        removeNonMasterAgents();
        xmlRpcHelper.logout();

        super.tearDown();
    }

    private void removeNonMasterAgents() throws Exception
    {
        // Ensure only the master agent is defined.
        Vector<String> allAgents = xmlRpcHelper.getAllAgentNames();
        for (String agent: allAgents)
        {
            if (!agent.equals(AgentManager.MASTER_AGENT_NAME))
            {
                xmlRpcHelper.deleteConfig(PathUtils.getPath(MasterConfigurationRegistry.AGENTS_SCOPE, agent));
            }
        }
    }

    public void testOfflineAgent() throws Exception
    {
        final String AGENT = "offline-agent";

        configurationHelper.insertAgent(new AgentConfiguration(AGENT, HOST_LOCALHOST, 8899));

        loginAsAdmin();
        AgentsPage agentsPage = browser.openAndWaitFor(AgentsPage.class);
        browser.refreshUntilText(agentsPage.getStatusId(AGENT), OFFLINE.getPrettyString());
        assertEquals(asList(ACTION_DISABLE, ACTION_PING), agentsPage.getActions(AGENT));
    }

    public void testDisableEnable() throws Exception
    {
        configurationHelper.insertAgent(new AgentConfiguration(LOCAL_AGENT, HOST_LOCALHOST, 8890));

        loginAsAdmin();
        AgentsPage agentsPage = browser.openAndWaitFor(AgentsPage.class);
        assertTrue(agentsPage.isActionAvailable(LOCAL_AGENT, ACTION_DISABLE));
        agentsPage.clickAction(LOCAL_AGENT, ACTION_DISABLE);

        browser.refreshUntilText(agentsPage.getStatusId(LOCAL_AGENT), DISABLED.getPrettyString());

        assertTrue(agentsPage.isActionAvailable(LOCAL_AGENT, ACTION_ENABLE));
        agentsPage.clickAction(LOCAL_AGENT, ACTION_ENABLE);
        
        browser.refreshUntilText(agentsPage.getStatusId(LOCAL_AGENT), IDLE.getPrettyString());
    }

    public void testDisableOnIdle() throws Exception
    {
        configurationHelper.insertAgent(new AgentConfiguration(LOCAL_AGENT, HOST_LOCALHOST, 8890));

        WaitProject project = projects.createWaitAntProject(tempDir, randomName());
        project.getDefaultStage().setAgent(configurationHelper.getAgentReference(LOCAL_AGENT));
        configurationHelper.insertProject(project.getConfig());

        xmlRpcHelper.waitForProjectToInitialise(project.getName());
        xmlRpcHelper.triggerBuild(project.getName());
        xmlRpcHelper.waitForBuildInProgress(project.getName(), 1);

        loginAsAdmin();
        AgentsPage agentsPage = browser.openAndWaitFor(AgentsPage.class);
        agentsPage.clickAction(LOCAL_AGENT, ACTION_DISABLE);

        browser.refreshUntilText(agentsPage.getStatusId(LOCAL_AGENT), STATUS_DISABLE_ON_IDLE);

        assertTrue(agentsPage.isActionAvailable(LOCAL_AGENT, ACTION_ENABLE));
        assertTrue(agentsPage.isActionAvailable(LOCAL_AGENT, ACTION_DISABLE_NOW));
        agentsPage.clickAction(LOCAL_AGENT, ACTION_ENABLE);
        agentsPage.clickAction(LOCAL_AGENT, ACTION_PING);

        browser.refreshUntilText(agentsPage.getStatusId(LOCAL_AGENT), BUILDING.getPrettyString(),RECIPE_ASSIGNED.getPrettyString());

        assertTrue(xmlRpcHelper.getAgentState(LOCAL_AGENT).isEnabled());

        agentsPage.clickAction(LOCAL_AGENT, ACTION_DISABLE);

        browser.refreshUntilText(agentsPage.getStatusId(LOCAL_AGENT), STATUS_DISABLE_ON_IDLE);

        assertTrue(xmlRpcHelper.getAgentState(LOCAL_AGENT).isDisabling());

        project.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(project.getName(), 1);

        browser.refreshUntilText(agentsPage.getStatusId(LOCAL_AGENT), DISABLED.getPrettyString());

        assertTrue(xmlRpcHelper.getAgentState(LOCAL_AGENT).isDisabled());
    }

    public void testConcurrentBuildsSameHost() throws Exception
    {
        String agent1 = LOCAL_AGENT + "-1";
        String agent2 = LOCAL_AGENT + "-2";

        configurationHelper.insertAgent( new AgentConfiguration(agent1, HOST_LOCALHOST, 8890));
        configurationHelper.insertAgent(new AgentConfiguration(agent2, HOST_LOCALHOST, 8890));

        String random = randomName();
        WaitProject project1 = projects.createWaitAntProject(tempDir, random + "-1");
        WaitProject project2 = projects.createWaitAntProject(tempDir, random + "-2");
        project1.getDefaultStage().setAgent(configurationHelper.getAgentReference(agent1));
        project2.getDefaultStage().setAgent(configurationHelper.getAgentReference(agent2));
        configurationHelper.insertProject(project1.getConfig());
        configurationHelper.insertProject(project2.getConfig());
        xmlRpcHelper.waitForProjectToInitialise(project1.getName());
        xmlRpcHelper.waitForProjectToInitialise(project2.getName());
        xmlRpcHelper.triggerBuild(project1.getName());
        xmlRpcHelper.waitForBuildInProgress(project1.getName(), 1);
        xmlRpcHelper.triggerBuild(project2.getName());
        xmlRpcHelper.waitForBuildInProgress(project2.getName(), 1);

        loginAsAdmin();
        AgentsPage agentsPage = browser.openAndWaitFor(AgentsPage.class);
        assertBuildingStatus(agentsPage.getStatus(agent1));
        assertBuildingStatus(agentsPage.getStatus(agent2));

        project2.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(project2.getName(), 1);

        browser.refresh();
        agentsPage.waitFor();
        assertBuildingStatus(agentsPage.getStatus(agent1));
        browser.refreshUntilText(agentsPage.getStatusId(agent2), IDLE.getPrettyString());
        
        project1.releaseBuild();
        browser.refreshUntilText(agentsPage.getStatusId(agent1), IDLE.getPrettyString());
    }

    public void testHostOptionProvider() throws Exception
    {
        final String HOST_1 = "host1";
        final String HOST_2 = "host2";

        String random = randomName();
        String agent1 = random + "-1";
        String agent2 = random + "-2";
        String agent3 = random + "-3";

        configurationHelper.insertAgent( new AgentConfiguration(agent1, HOST_1, 8890));
        configurationHelper.insertAgent( new AgentConfiguration(agent2, HOST_2, 8890));
        configurationHelper.insertAgent( new AgentConfiguration(agent3, HOST_1, 8890));

        loginAsAdmin();

        AgentHierarchyPage globalAgentPage = browser.openAndWaitFor(AgentHierarchyPage.class, AgentManager.GLOBAL_AGENT_NAME, true);
        globalAgentPage.clickAdd();

        AgentForm agentForm = browser.createForm(AgentForm.class, true);
        agentForm.waitFor();
        assertEquals(asList("", HOST_1, HOST_2), asList(agentForm.getComboBoxOptions("host")));
    }

    public void testAgentStatusExecutingBuild() throws Exception
    {
        loginAsAdmin();
        AgentStatusPage statusPage = browser.openAndWaitFor(AgentStatusPage.class, AgentManager.MASTER_AGENT_NAME);
        assertFalse(statusPage.isExecutingBuildPresent());

        WaitProject project = projects.createWaitAntProject(tempDir, random);
        project.getDefaultStage().setAgent(configurationHelper.getAgentReference(AgentManager.MASTER_AGENT_NAME));
        configurationHelper.insertProject(project.getConfig());
        xmlRpcHelper.waitForProjectToInitialise(project.getName());
        xmlRpcHelper.triggerBuild(project.getName());
        xmlRpcHelper.waitForBuildInProgress(project.getName(), 1);

        browser.refreshUntilElement(AgentStatusPage.ID_BUILD_TABLE);
        assertEquals(project.getName(), statusPage.getExecutingProject());
        assertEquals(project.getName(), statusPage.getExecutingOwner());
        assertEquals("1", statusPage.getExecutingId());
        assertEquals("default", statusPage.getExecutingStage());
        assertEquals("[default]", statusPage.getExecutingRecipe());

        project.releaseBuild();
        xmlRpcHelper.waitForBuildToComplete(project.getName(), 1);
    }

    public void testStatistics() throws Exception
    {
        loginAsAdmin();
        AgentStatisticsPage statisticsPage = browser.openAndWaitFor(AgentStatisticsPage.class, AgentManager.MASTER_AGENT_NAME);
        assertTrue(statisticsPage.isRecipeStatisticsPresent());
        assertTrue(statisticsPage.isUsageStatisticsPresent());
        assertTrue(statisticsPage.isUsageChartPresent());
    }

    private void assertBuildingStatus(String status)
    {
        assertTrue("Status '" + status + "' is not a building status", status.equals(BUILDING.getPrettyString()) || status.equals(RECIPE_ASSIGNED.getPrettyString()));
    }
}
