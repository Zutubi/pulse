package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.acceptance.components.table.KeyValueTable;
import com.zutubi.pulse.acceptance.components.table.PropertyTable;
import com.zutubi.pulse.acceptance.pages.agents.AgentStatusPage;
import com.zutubi.pulse.acceptance.pages.agents.ExecutingStageTable;
import com.zutubi.pulse.acceptance.pages.agents.SynchronisationMessageTable;
import com.zutubi.pulse.acceptance.utils.*;
import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.master.agent.AgentManager;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;
import com.zutubi.pulse.master.tove.config.agent.AgentConfigurationActions;
import com.zutubi.pulse.servercore.agent.SynchronisationTask;
import com.zutubi.util.Condition;
import com.zutubi.util.FileSystemUtils;

import java.io.File;

import static com.zutubi.pulse.master.agent.AgentSynchronisationService.COMPLETED_MESSAGE_LIMIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * Tests for the agent status tab.
 */
public class AgentStatusAcceptanceTest extends AcceptanceTestBase
{
    private static final String TEST_DESCRIPTION = "test description";

    private ConfigurationHelper configurationHelper;
    private ProjectConfigurations projects;
    private File tempDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        rpcClient.loginAsAdmin();
        rpcClient.cancelIncompleteBuilds();
        removeNonMasterAgents();

        ConfigurationHelperFactory factory = new SingletonConfigurationHelperFactory();
        configurationHelper = factory.create(rpcClient.RemoteApi);
        projects = new ProjectConfigurations(configurationHelper);

        tempDir = FileSystemUtils.createTempDir(getName());
    }

    @Override
    protected void tearDown() throws Exception
    {
        rpcClient.cancelIncompleteBuilds();
        rpcClient.logout();

        removeDirectory(tempDir);

        super.tearDown();
    }

    public void testAgentStatusBasics() throws Exception
    {
        getBrowser().loginAsAdmin();
        final AgentStatusPage statusPage = getBrowser().openAndWaitFor(AgentStatusPage.class, AgentManager.MASTER_AGENT_NAME);
        PropertyTable infoTable = statusPage.getInfoTable();
        assertTrue(infoTable.isPresent());
        assertEquals(AgentManager.MASTER_AGENT_NAME, infoTable.getValue("name"));
        assertEquals("[master]", infoTable.getValue("location"));

        KeyValueTable statusTable = statusPage.getStatusTable();
        assertTrue(statusTable.isPresent());
        assertTrue(statusTable.getKeyValuePairs().containsKey("time of last ping"));
    }
    
    public void testAgentStatusExecutingBuild() throws Exception
    {
        getBrowser().loginAsAdmin();
        final AgentStatusPage statusPage = getBrowser().openAndWaitFor(AgentStatusPage.class, AgentManager.MASTER_AGENT_NAME);
        if (statusPage.getExecutingStageTable().isPresent())
        {
            getBrowser().refreshUntil(SeleniumBrowser.REFRESH_TIMEOUT, new Condition()
            {
                public boolean satisfied()
                {
                    statusPage.waitFor();
                    return !statusPage.getExecutingStageTable().isPresent();
                }
            }, "executing build to stop on master agent");
        }

        WaitProject project = startBuildOnAgent(random, AgentManager.MASTER_AGENT_NAME);

        getBrowser().refreshUntil(SeleniumBrowser.REFRESH_TIMEOUT, new Condition()
        {
            public boolean satisfied()
            {
                statusPage.waitFor();
                return statusPage.getExecutingStageTable().isPresent();
            }
        }, "executing build to start on master agent");

        ExecutingStageTable executingStageTable = statusPage.getExecutingStageTable();
        assertTrue(executingStageTable.isPresent());
        assertEquals(project.getName(), executingStageTable.getProject());
        assertEquals(project.getName(), executingStageTable.getOwner());
        assertEquals(1L, executingStageTable.getNumber());
        assertEquals("default", executingStageTable.getStage());
        assertEquals("[default]", executingStageTable.getRecipe());

        project.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), 1);
    }

    public void testActions() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleAgent(random, "localhost");
        getBrowser().loginAsAdmin();
        final AgentStatusPage statusPage = getBrowser().openAndWaitFor(AgentStatusPage.class, random);
        assertTrue(statusPage.isActionPresent(AgentConfigurationActions.ACTION_DISABLE));
        statusPage.clickAction(AgentConfigurationActions.ACTION_DISABLE);
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return statusPage.isActionPresent(AgentConfigurationActions.ACTION_ENABLE);
            }
        }, SeleniumBrowser.DEFAULT_TIMEOUT, "agent to disable");
        
        statusPage.clickAction(AgentConfigurationActions.ACTION_ENABLE);
        TestUtils.waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return statusPage.isActionPresent(AgentConfigurationActions.ACTION_DISABLE);
            }
        }, SeleniumBrowser.DEFAULT_TIMEOUT, "agent to enable");

    }

    public void testNoSynchronisationMessages() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleAgent(random, "localhost");
        getBrowser().loginAsAdmin();
        AgentStatusPage statusPage = getBrowser().openAndWaitFor(AgentStatusPage.class, random);
        SynchronisationMessageTable messagesTable = statusPage.getSynchronisationMessagesTable();
        assertTrue(getBrowser().isElementIdPresent(messagesTable.getId()));
        assertEquals(0, messagesTable.getRowCount());
        assertTrue(getBrowser().isTextPresent("no synchronisation messages found"));
    }

    public void testSimpleSynchronisationMessage() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleAgent(random, "localhost");
        rpcClient.RemoteApi.waitForAgentToBeIdle(random);
        rpcClient.TestApi.enqueueSynchronisationMessage(random, true, "test message", true);
        rpcClient.RemoteApi.waitForAgentToBeIdle(random);

        getBrowser().loginAsAdmin();
        AgentStatusPage statusPage = getBrowser().openAndWaitFor(AgentStatusPage.class, random);
        SynchronisationMessageTable messagesTable = statusPage.getSynchronisationMessagesTable();
        assertEquals(1, messagesTable.getRowCount());
        SynchronisationMessageTable.SynchronisationMessage expectedMessage = new SynchronisationMessageTable.SynchronisationMessage(SynchronisationTask.Type.TEST, "test message", AgentSynchronisationMessage.Status.SUCCEEDED);
        assertEquals(expectedMessage, messagesTable.getMessage(0));
    }

    public void testAsyncSynchronisationMessage() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleAgent(random, "localhost");
        rpcClient.RemoteApi.waitForAgentToBeIdle(random);
        rpcClient.TestApi.enqueueSynchronisationMessage(random, false, "test async message", true);
        rpcClient.RemoteApi.waitForAgentToBeIdle(random);

        getBrowser().loginAsAdmin();
        AgentStatusPage statusPage = getBrowser().openAndWaitFor(AgentStatusPage.class, random);
        SynchronisationMessageTable messagesTable = statusPage.getSynchronisationMessagesTable();
        assertEquals(1, messagesTable.getRowCount());
        SynchronisationMessageTable.SynchronisationMessage expectedMessage = new SynchronisationMessageTable.SynchronisationMessage(SynchronisationTask.Type.TEST_ASYNC, "test async message", AgentSynchronisationMessage.Status.SUCCEEDED);
        assertEquals(expectedMessage, messagesTable.getMessage(0));
    }
    
    public void testFailedSynchronisationMessage() throws Exception
    {
        rpcClient.RemoteApi.insertSimpleAgent(random, "localhost");
        rpcClient.RemoteApi.waitForAgentToBeIdle(random);
        rpcClient.TestApi.enqueueSynchronisationMessage(random, true, TEST_DESCRIPTION, false);
        rpcClient.RemoteApi.waitForAgentToBeIdle(random);

        getBrowser().loginAsAdmin();
        AgentStatusPage statusPage = getBrowser().openAndWaitFor(AgentStatusPage.class, random);
        SynchronisationMessageTable messagesTable = statusPage.getSynchronisationMessagesTable();
        assertEquals(1, messagesTable.getRowCount());
        SynchronisationMessageTable.SynchronisationMessage expectedMessage = new SynchronisationMessageTable.SynchronisationMessage(SynchronisationTask.Type.TEST, TEST_DESCRIPTION, AgentSynchronisationMessage.Status.FAILED_PERMANENTLY);
        assertEquals(expectedMessage, messagesTable.getMessage(0));
        String statusMessage = messagesTable.clickAndWaitForMessageStatus(0);
        assertThat(statusMessage, containsString("Test failure."));
    }

    public void testSynchronisationMessageQueuedWhileBuilding() throws Exception
    {
        String agentName = random + "-agent";
        String projectName = random + "-project";

        rpcClient.RemoteApi.insertSimpleAgent(agentName, "localhost");

        WaitProject project = startBuildOnAgent(projectName, agentName);

        rpcClient.TestApi.enqueueSynchronisationMessage(agentName, true, TEST_DESCRIPTION, true);

        getBrowser().loginAsAdmin();
        AgentStatusPage statusPage = getBrowser().openAndWaitFor(AgentStatusPage.class, agentName);
        SynchronisationMessageTable messagesTable = statusPage.getSynchronisationMessagesTable();
        assertEquals(1, messagesTable.getRowCount());
        SynchronisationMessageTable.SynchronisationMessage expectedMessage = new SynchronisationMessageTable.SynchronisationMessage(SynchronisationTask.Type.TEST, TEST_DESCRIPTION, AgentSynchronisationMessage.Status.QUEUED);
        assertEquals(expectedMessage, messagesTable.getMessage(0));

        project.releaseBuild();
        rpcClient.RemoteApi.waitForBuildToComplete(project.getName(), 1);
        rpcClient.RemoteApi.waitForAgentToBeIdle(agentName);

        statusPage.openAndWaitFor();
        assertEquals(1, messagesTable.getRowCount());
        expectedMessage = new SynchronisationMessageTable.SynchronisationMessage(SynchronisationTask.Type.TEST, TEST_DESCRIPTION, AgentSynchronisationMessage.Status.SUCCEEDED);
        assertEquals(expectedMessage, messagesTable.getMessage(0));
    }

    public void testMultipleSynchronisationMessages() throws Exception
    {
        final int MESSAGE_COUNT = 12;

        rpcClient.RemoteApi.insertSimpleAgent(random, "localhost");
        multipleMessageHelper(random, MESSAGE_COUNT);
    }

    public void testMultipleSynchronisationMessagesOnMaster() throws Exception
    {
        final int MESSAGE_COUNT = 12;

        rpcClient.RemoteApi.insertLocalAgent(random);
        multipleMessageHelper(random, MESSAGE_COUNT);
    }

    private void multipleMessageHelper(String agentName, int messageCount) throws Exception
    {
        rpcClient.RemoteApi.waitForAgentToBeIdle(agentName);
        for (int i = 0; i < messageCount; i++)
        {
            rpcClient.TestApi.enqueueSynchronisationMessage(agentName, true, "Description " + i, true);
        }
        rpcClient.RemoteApi.waitForAgentToBeIdle(agentName);

        getBrowser().loginAsAdmin();
        AgentStatusPage statusPage = getBrowser().openAndWaitFor(AgentStatusPage.class, agentName);
        SynchronisationMessageTable messagesTable = statusPage.getSynchronisationMessagesTable();
        assertEquals(COMPLETED_MESSAGE_LIMIT, messagesTable.getRowCount());
        for (int i = 0; i < COMPLETED_MESSAGE_LIMIT; i++)
        {
            SynchronisationMessageTable.SynchronisationMessage expectedMessage = new SynchronisationMessageTable.SynchronisationMessage(SynchronisationTask.Type.TEST, "Description " + (messageCount - i - 1), AgentSynchronisationMessage.Status.SUCCEEDED);
            assertEquals(expectedMessage, messagesTable.getMessage(i));
        }
    }

    private WaitProject startBuildOnAgent(String projectName, String agentName) throws Exception
    {
        WaitProject project = projects.createWaitAntProject(projectName, tempDir, false);
        project.getDefaultStage().setAgent(configurationHelper.getAgentReference(agentName));
        configurationHelper.insertProject(project.getConfig(), false);
        rpcClient.RemoteApi.waitForProjectToInitialise(project.getName());
        rpcClient.RemoteApi.triggerBuild(project.getName());
        rpcClient.RemoteApi.waitForBuildInProgress(project.getName(), 1);
        return project;
    }
}
