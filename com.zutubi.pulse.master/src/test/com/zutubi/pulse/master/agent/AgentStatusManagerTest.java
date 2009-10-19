package com.zutubi.pulse.master.agent;

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.engine.api.Property;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.AgentService;
import com.zutubi.pulse.master.events.*;
import com.zutubi.pulse.master.events.build.*;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.tove.config.admin.AgentPingConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.agent.PingStatus;
import com.zutubi.pulse.servercore.agent.Status;
import com.zutubi.pulse.servercore.services.SlaveStatus;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.Pair;
import com.zutubi.util.Predicate;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

public class AgentStatusManagerTest extends PulseTestCase implements EventListener
{
    private EventManager eventManager;
    private AgentStatusManager agentStatusManager;
    private AgentPingConfiguration agentPingConfiguration = new AgentPingConfiguration();
    private List<Event> receivedEvents = new LinkedList<Event>();
    private List<Pair<Long, AgentState.EnableState>> enableStates = new LinkedList<Pair<Long, AgentState.EnableState>>();

    protected void setUp() throws Exception
    {
        super.setUp();
        eventManager = new DefaultEventManager();
        eventManager.register(this);

        AgentPersistentStatusManager agentPersistentStatusManager = new AgentPersistentStatusManager()
        {
            public void setEnableState(Agent agent, AgentState.EnableState state)
            {
                enableStates.add(new Pair<Long, AgentState.EnableState>(agent.getId(), state));
                AgentState agentState = createAgentState(agent.getId());
                agentState.setEnableState(state);
                agent.setAgentState(agentState);
            }
        };

        agentStatusManager = new AgentStatusManager(agentPersistentStatusManager, new Executor()
        {
            public void execute(Runnable command)
            {
                // Pump events synchronously to avoid test races.  The status
                // manager itself never handles them so it matters not to the
                // accuracy of the tests.
                command.run();
            }
        }, eventManager);

        ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
        stub(configurationProvider.get(AgentPingConfiguration.class)).toReturn(agentPingConfiguration);
        agentStatusManager.setConfigurationProvider(configurationProvider);

        // For a little realism, create a "master" agent.
        Agent a = addAgent(0);
        sendPing(a, new SlaveStatus(PingStatus.IDLE));
        clearEvents();
        
        setTimeout(60);
    }

    protected void tearDown() throws Exception
    {
        eventManager = null;
        agentStatusManager = null;
        receivedEvents = null;
    }

    public void testAddAgent()
    {
        Agent agent = addAgent(1);

        List<Agent> allAgents = agentStatusManager.getAllAgents();
        assertEquals(2, allAgents.size());
        assertTrue(allAgents.contains(agent));

        List<Agent> onlineAgents = getOnlineAgents();
        assertEquals(1, onlineAgents.size());
        assertFalse(onlineAgents.contains(agent));

        onComplete();
    }

    public void testOnlineAvailableAgent()
    {
        Agent agent = addAgent(1);
        clearEvents();
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));

        assertOfflineToAvailableEvents(agent);

        List<Agent> onlineAgents = getOnlineAgents();
        assertEquals(2, onlineAgents.size());
        assertTrue(onlineAgents.contains(agent));

        onComplete();
    }

    public void testAgentOffline()
    {
        Agent agent = addAgent(1);
        clearEvents();
        sendPing(agent, new SlaveStatus(PingStatus.OFFLINE));
        sendPing(agent, new SlaveStatus(PingStatus.OFFLINE));

        onComplete();
    }

    public void testAgentExecuteRecipe()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testAgentRecipeError()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();

        sendRecipeCollecting(1000);
        assertNoEvents();
        assertEquals(Status.POST_RECIPE, agent.getStatus());
        collectRecipe(agent, 1000);

        onComplete();
    }

    public void testAgentExecuteRecipeNoBuildingPing()
    {
        // We should handle going directly from recipe assigned to a recipe
        // complete event.
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testAgentImmediateRecipeError()
    {
        // We should handle going directly from recipe assigned to a recipe
        // error event.
        Agent agent = addAgentAndAssignRecipe(1, 1000);

        sendRecipeCollecting(1000);
        assertNoEvents();
        assertEquals(Status.POST_RECIPE, agent.getStatus());
        collectRecipe(agent, 1000);

        onComplete();
    }

    public void testRecipeAssignedIdlePingRace()
    {
        // It is valid to see an idle ping post recipe dispatch.
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        sendRecipeAssigned(agent, 1000);
        clearEvents();

        sendPing(agent, new SlaveStatus(PingStatus.IDLE, 1000, false));
        assertNoEvents();
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        assertNoEvents();
        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testRecipeAssignedNeverBuilding()
    {
        // If the agent keeps returning idle pings after the timeout post
        // recipe dispatch, then we assume it is not a race but an error.
        lowerTimeout();
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        sendRecipeAssigned(agent, 1000);
        clearEvents();
        waitForTimeout();
        sendPing(agent, new SlaveStatus(PingStatus.IDLE, 1000, false));
        assertEvents(new RecipeErrorEvent(this, 1000, "Agent idle after recipe expected to have commenced (agent: agent 1, recipe: 1000, since ping: 2, timeout: 1)"));
        sendRecipeCollecting(1000);

        onComplete();
    }

    public void testPostRecipePings()
    {
        // Post stage actions are free to fiddle with agents in the post
        // recipe state, so no ping will have an affect.
        Agent agent = addAgentAndAssignRecipe(1, 1000);

        sendRecipeCollecting(1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        assertNoEvents();
        assertEquals(Status.POST_RECIPE, agent.getStatus());
        sendPing(agent, new SlaveStatus(PingStatus.IDLE, 1000, false));
        assertNoEvents();
        assertEquals(Status.POST_RECIPE, agent.getStatus());
        sendPing(agent, new SlaveStatus(PingStatus.OFFLINE, 1000, false));
        assertNoEvents();
        assertEquals(Status.POST_RECIPE, agent.getStatus());
        sendPing(agent, new SlaveStatus(PingStatus.VERSION_MISMATCH, 1000, false));
        assertNoEvents();
        assertEquals(Status.POST_RECIPE, agent.getStatus());

        onComplete();
    }

    public void testAwaitingPingBuildingPingRace()
    {
        // Although unlikely, we could see building pings for the previous
        // recipe in the awaiting ping state.  These should be ignored as
        // they are racey.
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendRecipeCollecting(1000);
        sendRecipeCollected(1000);
        clearEvents();
        assertEquals(Status.AWAITING_PING, agent.getStatus());
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        assertNoEvents();
        assertEquals(Status.AWAITING_PING, agent.getStatus());

        onComplete();
    }

    public void testAwaitingPingBuildingPingTimeout()
    {
        // If building pings for the previous recipe continue to arrive while
        // we are in the awaiting ping state, which should eventually timeout
        // and try and regain the agent's attention.
        lowerTimeout();
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendRecipeCollecting(1000);
        sendRecipeCollected(1000);
        clearEvents();
        assertEquals(Status.AWAITING_PING, agent.getStatus());
        assertNoEvents();
        waitForTimeout();
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        assertEquals(Status.BUILDING_INVALID, agent.getStatus());
        assertEvents(new RecipeTerminateRequestEvent(this, null, 1000));

        onComplete();
    }

    public void testIntermittentOfflineDuringRecipe()
    {
        // One offline ping is not enough to kill a recipe.
        Agent agent = addAgentAndAssignRecipe(1, 1000);

        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendPing(agent, new SlaveStatus(PingStatus.OFFLINE));
        assertNoEvents();
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        assertNoEvents();
        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testOfflineDuringRecipe()
    {
        // Continued offline pings pass the timeout should kill a recipe.
        lowerTimeout();
        Agent agent = addAgentAndAssignRecipe(1, 1000);

        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        waitForTimeout();
        sendPing(agent, new SlaveStatus(PingStatus.OFFLINE));
        assertEvents(
                new RecipeErrorEvent(this, 1000, "Connection to agent lost during recipe execution (agent: agent 1, recipe: 1000, since ping: 2, timeout: 1)"),
                new AgentOfflineEvent(this, agent)
        );
        sendRecipeCollecting(1000);

        onComplete();
    }

    public void testRecipeMismatch()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);

        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 2000, false));
        assertEvents(new RecipeErrorEvent(this, 1000, "Agent recipe mismatch"));
        sendRecipeCollecting(1000);

        // We still go through the normal post-recipe state for recipe 1000.
        assertEquals(Status.POST_RECIPE, agent.getStatus());
        assertEvents(new RecipeTerminateRequestEvent(this, null, 2000));

        // Assume for this test that the terminate makes the agent idle for
        // the next ping.
        collectRecipe(agent, 1000);

        onComplete();
    }

    public void testRecipeMismatchTerminateFails()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);

        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 2000, false));
        assertEvents(new RecipeErrorEvent(this, 1000, "Agent recipe mismatch"));
        sendRecipeCollecting(1000);

        // We still go through the normal post-recipe state for recipe 1000.
        assertEquals(Status.POST_RECIPE, agent.getStatus());
        assertEvents(new RecipeTerminateRequestEvent(this, null, 2000));

        // What happens if the terminate failed and the wretched thing is
        // still building the wrong recipe?
        sendRecipeCollected(1000);
        assertEquals(Status.AWAITING_PING, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 2000, false));
        assertEquals(Status.BUILDING_INVALID, agent.getStatus());
        assertEvents(new RecipeTerminateRequestEvent(this, null, 2000));

        onComplete();
    }

    public void testBuildingInvalidCompletes()
    {
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();
        assertEquals(Status.BUILDING_INVALID, agent.getStatus());

        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        assertEvents(new AgentAvailableEvent(this, agent));

        onComplete();
    }

    public void testMasterBounce()
    {
        // If the master bounces, it should try to resync an agent that is
        // building a defunct recipe.
        Agent agent = addAgent(1);
        clearEvents();
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        assertEvents(
                new RecipeTerminateRequestEvent(this, null, 1000),
                new AgentOnlineEvent(this, agent)
        );
        assertEquals(Status.BUILDING_INVALID, agent.getStatus());

        onComplete();
    }

    public void testAgentBounce()
    {
        // If an agent bounces between pings, we should act like we saw it
        // offline.
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        clearEvents();

        sendPing(agent, new SlaveStatus(PingStatus.IDLE, -1, true));
        assertAvailableToOfflineEvents(agent);
        assertOfflineToAvailableEvents(agent);
        assertEquals(Status.IDLE, agent.getStatus());

        onComplete();
    }

    public void testDisableOfflineAgent()
    {
        addAgentAndDisable(1);

        onComplete();
    }

    public void testPingDisabled()
    {
        Agent agent = addAgentAndDisable(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));

        onComplete();
    }

    public void testDisableOnlineAgent()
    {
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        clearEvents();

        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLED);
        assertAvailableToOfflineEvents(agent);

        onComplete();
    }

    public void testDisableRecipeAssignedAgent()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        clearEvents();

        completeDisableOnIdleBuild(agent);

        onComplete();
    }

    public void testDisableBuildingAgent()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();

        completeDisableOnIdleBuild(agent);

        onComplete();
    }

    private void completeDisableOnIdleBuild(Agent agent)
    {
        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLING);
        sendRecipeCollecting(1000);
        sendRecipeCollected(1000);
        assertBusyAgentDisabled(agent);
    }

    public void testDisablePostRecipeAgent()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendRecipeCollecting(1000);
        clearEvents();

        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLING);
        sendRecipeCollected(1000);

        assertBusyAgentDisabled(agent);

        onComplete();
    }

    public void testDisableAwaitingPingAgent()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendRecipeCollecting(1000);
        sendRecipeCollected(1000);
        assertEquals(Status.AWAITING_PING, agent.getStatus());
        clearEvents();

        sendDisableRequest(agent);
        assertBusyAgentDisabled(agent);

        onComplete();
    }

    public void testHardDisableRecipeAssignedAgent()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLING);

        hardDisableBuilding(agent);

        onComplete();
    }

    public void testHardDisableBuildingAgent()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();

        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLING);

        hardDisableBuilding(agent);

        onComplete();
    }

    private void hardDisableBuilding(Agent agent)
    {
        sendDisableRequest(agent);
        assertEvents(
                new RecipeTerminateRequestEvent(this, null, 1000),
                new RecipeErrorEvent(this, 1000, "Agent disabled while recipe in progress")
        );
        sendRecipeCollecting(1000);
        assertEquals(Status.POST_RECIPE, agent.getStatus());

        sendRecipeCollected(1000);
        assertBusyAgentDisabled(agent);
    }

    public void testHardDisablePostRecipeAgent()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendRecipeCollecting(1000);
        clearEvents();

        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLING);

        sendDisableRequest(agent);
        assertEquals(Status.POST_RECIPE, agent.getStatus());

        sendRecipeCollected(1000);
        assertBusyAgentDisabled(agent);

        onComplete();
    }

    public void testHardDisableAtDifferentStages()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();
        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLING);

        sendDisableRequest(agent);
        assertEvents(
                new RecipeTerminateRequestEvent(this, null, 1000),
                new RecipeErrorEvent(this, 1000, "Agent disabled while recipe in progress")
        );
        sendRecipeCollecting(1000);
        assertEquals(Status.POST_RECIPE, agent.getStatus());

        sendRecipeCollecting(1000);
        sendRecipeCollected(1000);
        assertBusyAgentDisabled(agent);

        onComplete();
    }

    public void testDisableBuildingInvalidAgent()
    {
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();

        assertEquals(Status.BUILDING_INVALID, agent.getStatus());

        sendDisableRequest(agent);
        assertEquals(Status.BUILDING_INVALID, agent.getStatus());
        assertEnableState(agent, AgentState.EnableState.DISABLING);

        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        assertAgentDisabled(agent);

        assertEvents(new AgentOfflineEvent(this, agent));

        onComplete();
    }

    public void testEnableAgent()
    {
        Agent agent = addAgentAndDisable(1);
        sendEnableRequest(agent);
        assertEquals(Status.INITIAL, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));
        assertEnableState(agent, AgentState.EnableState.ENABLED);

        onComplete();
    }

    public void testEnabledAgentComesOnline()
    {
        Agent agent = addAgentAndDisable(1);
        sendEnableRequest(agent);
        assertEquals(Status.INITIAL, agent.getStatus());
        assertEnableState(agent, AgentState.EnableState.ENABLED);
        assertEvents(new AgentPingRequestedEvent(this, agent));

        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        assertOfflineToAvailableEvents(agent);

        onComplete();
    }

    public void testEnableDisabling()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));

        sendDisableRequest(agent);
        assertEquals(Status.BUILDING, agent.getStatus());
        assertEnableState(agent, AgentState.EnableState.DISABLING);
        assertNoEvents();

        sendEnableRequest(agent);
        assertEquals(Status.BUILDING, agent.getStatus());
        assertEnableState(agent, AgentState.EnableState.ENABLED);
        assertNoEvents();

        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testRemoveOfflineAgent()
    {
        Agent agent = addAgent(1);
        clearEvents();
        removeAgent(agent);

        assertSlaveRemoved(agent);

        onComplete();
    }

    public void testRemoveAvailableAgent()
    {
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        clearEvents();
        removeAgent(agent);

        assertAvailableToOfflineEvents(agent);

        List<Agent> onlineAgents = getOnlineAgents();
        assertEquals(1, onlineAgents.size());
        assertFalse(onlineAgents.contains(agent));

        assertSlaveRemoved(agent);

        onComplete();
    }

    public void testRemoveRecipeAssignedAgent()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        clearEvents();
        removeAgent(agent);

        removedDuringBuildHelper(agent);

        onComplete();
    }

    public void testRemoveBuildingAgent()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();
        removeAgent(agent);

        removedDuringBuildHelper(agent);

        onComplete();
    }

    private void removedDuringBuildHelper(Agent agent)
    {
        // The manager should kill off the recipe controller, but ignore the
        // agent as it is now meaningless to us.
        assertEvents(new RecipeErrorEvent(this, 1000, "Agent deleted while recipe in progress"), new AgentOfflineEvent(this, agent));
        sendRecipeCollecting(1000);

        assertSlaveRemoved(agent);
    }

    public void testRemovePostRecipeAgent()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendRecipeCollecting(1000);
        clearEvents();
        removeAgent(agent);

        assertEvents(new AgentOfflineEvent(this, agent));
        assertSlaveRemoved(agent);

        onComplete();
    }

    public void testRemoveAwaitingPingAgent()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendRecipeCollecting(1000);
        sendRecipeCollected(1000);
        clearEvents();
        removeAgent(agent);

        assertEvents(new AgentOfflineEvent(this, agent));
        assertSlaveRemoved(agent);

        onComplete();
    }

    public void testChangeOfflineAgent()
    {
        Agent agent = addAgent(1);
        clearEvents();
        Agent newAgent = changeAgent(agent);

        final List<Agent> allAgents = agentStatusManager.getAllAgents();
        assertFalse(allAgents.contains(agent));
        assertTrue(allAgents.contains(newAgent));

        onComplete();
    }

    public void testChangeAvailableAgent()
    {
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        clearEvents();
        Agent newAgent = changeAgent(agent);

        final List<Agent> allAgents = agentStatusManager.getAllAgents();
        assertFalse(allAgents.contains(agent));
        assertTrue(allAgents.contains(newAgent));

        onComplete();
    }

    public void testChangeBuildingAgent()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();
        Agent newAgent = changeAgent(agent);

        assertNoEvents();
        completeAndCollectRecipe(newAgent, 1000);

        onComplete();
    }

    public void testVersionMismatch()
    {
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.VERSION_MISMATCH));

        assertEquals(Status.VERSION_MISMATCH, agent.getStatus());
        assertEvents(new AgentUpgradeRequiredEvent(this, agent));

        onComplete();
    }

    public void testVersionMismatchAvailable()
    {
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        clearEvents();
        sendPing(agent, new SlaveStatus(PingStatus.VERSION_MISMATCH));

        assertEquals(Status.VERSION_MISMATCH, agent.getStatus());
        assertAvailableToOfflineEvents(agent);
        assertEvents(new AgentUpgradeRequiredEvent(this, agent));

        onComplete();
    }

    public void testVersionMismatchBuilding()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();
        sendPing(agent, new SlaveStatus(PingStatus.VERSION_MISMATCH));

        assertEquals(Status.VERSION_MISMATCH, agent.getStatus());
        assertEvents(
                new RecipeErrorEvent(this, 1000, "Agent status changed to 'version mismatch' while recipe in progress"),
                new AgentOfflineEvent(this, agent),
                new AgentUpgradeRequiredEvent(this, agent)
        );
        sendRecipeCollecting(1000);

        onComplete();
    }

    public void testTokenMismatch()
    {
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.TOKEN_MISMATCH));

        assertEquals(Status.TOKEN_MISMATCH, agent.getStatus());

        onComplete();
    }

    public void testTokenMismatchAvailable()
    {
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        clearEvents();
        sendPing(agent, new SlaveStatus(PingStatus.TOKEN_MISMATCH));

        assertEquals(Status.TOKEN_MISMATCH, agent.getStatus());
        assertAvailableToOfflineEvents(agent);

        onComplete();
    }

    public void testTokenMismatchBuilding()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();
        sendPing(agent, new SlaveStatus(PingStatus.TOKEN_MISMATCH));

        assertEquals(Status.TOKEN_MISMATCH, agent.getStatus());
        assertEvents(
                new RecipeErrorEvent(this, 1000, "Agent status changed to 'token mismatch' while recipe in progress"),
                new AgentOfflineEvent(this, agent)
        );
        sendRecipeCollecting(1000);

        onComplete();
    }

    public void testInvalidMaster()
    {
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.INVALID_MASTER));

        assertEquals(Status.INVALID_MASTER, agent.getStatus());

        onComplete();
    }

    public void testInvalidMasterAvailable()
    {
        Agent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        clearEvents();
        sendPing(agent, new SlaveStatus(PingStatus.INVALID_MASTER));

        assertEquals(Status.INVALID_MASTER, agent.getStatus());
        assertAvailableToOfflineEvents(agent);

        onComplete();
    }

    public void testInvalidMasterBuilding()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();
        sendPing(agent, new SlaveStatus(PingStatus.INVALID_MASTER));

        assertEquals(Status.INVALID_MASTER, agent.getStatus());
        assertEvents(
                new RecipeErrorEvent(this, 1000, "Agent status changed to 'invalid master' while recipe in progress"),
                new AgentOfflineEvent(this, agent)
        );
        sendRecipeCollecting(1000);

        onComplete();
    }

    public void testAbortAfterAssign()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendRecipeAborted(1000);
        assertEquals(Status.AWAITING_PING, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        assertEquals(Status.IDLE, agent.getStatus());
        assertEvents(new AgentAvailableEvent(this, agent));

        onComplete();
    }

    public void testAbortBuilding()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        assertEquals(Status.BUILDING, agent.getStatus());
        sendRecipeAborted(1000);
        assertEquals(Status.AWAITING_PING, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));

        onComplete();
    }

    public void testAbortDisabling()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendDisableRequest(agent);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        assertEnableState(agent, AgentState.EnableState.DISABLING);
        sendRecipeAborted(1000);
        assertBusyAgentDisabled(agent);

        onComplete();
    }

    public void testAbortCollecting()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendRecipeCollecting(1000);
        assertEquals(Status.POST_RECIPE, agent.getStatus());
        sendRecipeAborted(1000);
        assertEquals(Status.AWAITING_PING, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));

        onComplete();
    }
    
    public void testAbortIdle()
    {
        Agent agent = addAgentAndAssignRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendRecipeCollecting(1000);
        sendRecipeCollected(1000);
        assertEvents(new AgentPingRequestedEvent(this, agent));
        sendPing(agent, new SlaveStatus(PingStatus.IDLE, 1000, false));
        assertEquals(Status.IDLE, agent.getStatus());
        assertEvents(new AgentAvailableEvent(this, agent));
        sendRecipeAborted(1000);
        assertEquals(Status.IDLE, agent.getStatus());

        onComplete();
    }

    private void assertBusyAgentDisabled(Agent agent)
    {
        assertAgentDisabled(agent);
        assertEvents(new AgentOfflineEvent(this, agent));
    }

    private void assertAgentDisabled(Agent agent)
    {
        assertEquals(Status.DISABLED, agent.getStatus());
        assertEnableState(agent, AgentState.EnableState.DISABLED);
    }

    private void assertSlaveRemoved(Agent agent)
    {
        List<Agent> allAgents = agentStatusManager.getAllAgents();
        assertEquals(1, allAgents.size());
        assertFalse(allAgents.contains(agent));
    }

    private Agent addAgentAndAssignRecipe(int agentId, int recipeId)
    {
        Agent agent = addAgent(agentId);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        clearEvents();
        sendRecipeAssigned(agent, recipeId);
        assertEvents(new AgentUnavailableEvent(this, agent));
        assertEquals(Status.RECIPE_ASSIGNED, agent.getStatus());
        return agent;
    }

    private Agent addAgentAndDisable(int id)
    {
        Agent agent = addAgent(id);
        clearEvents();
        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLED);
        return agent;
    }

    private void completeAndCollectRecipe(Agent agent, int recipeId)
    {
        sendRecipeCollecting(recipeId);
        assertNoEvents();
        assertEquals(Status.POST_RECIPE, agent.getStatus());
        collectRecipe(agent, recipeId);
    }

    private void collectRecipe(Agent agent, int recipeId)
    {
        sendRecipeCollected(recipeId);
        assertEquals(Status.AWAITING_PING, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));
        assertNoEvents();
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        assertEvents(new AgentAvailableEvent(this, agent));
        assertEquals(Status.IDLE, agent.getStatus());

        assertNoEvents();
    }

    private void lowerTimeout()
    {
        setTimeout(1);
    }

    private void waitForTimeout()
    {
        try
        {
            Thread.sleep(2000);
        }
        catch (InterruptedException e)
        {
            // Empty
        }
    }

    private void setTimeout(int seconds)
    {
        agentPingConfiguration.setOfflineTimeout(seconds);
    }

    private List<Agent> getOnlineAgents()
    {
        return agentStatusManager.getAgentsByStatusPredicate(new Predicate<Status>()
        {
            public boolean satisfied(Status status)
            {
                return status.isOnline();
            }
        });
    }

    private Agent addAgent(int id)
    {
        Agent agent = createAgent(id);
        eventManager.publish(new AgentAddedEvent(this, agent));
        return agent;
    }

    private void removeAgent(Agent agent)
    {
        eventManager.publish(new AgentRemovedEvent(this, agent));
    }

    private Agent changeAgent(Agent agent)
    {
        agent = createAgent(agent.getId());
        eventManager.publish(new AgentChangedEvent(this, agent));
        return agent;
    }

    private void sendPing(Agent agent, SlaveStatus status)
    {
        eventManager.publish(new AgentPingEvent(this, agent, status));
    }

    private void sendRecipeAssigned(Agent agent, int recipeId)
    {
        PulseExecutionContext context = new PulseExecutionContext();
        context.add(BuildProperties.NAMESPACE_INTERNAL, new Property(BuildProperties.PROPERTY_RECIPE_ID, Long.toString(recipeId)));
        eventManager.publish(new RecipeAssignedEvent(this, new RecipeRequest(context), agent));
    }

    private void sendRecipeCollecting(int recipeId)
    {
        eventManager.publish(new RecipeCollectingEvent(this, recipeId));
    }

    private void sendRecipeCollected(int recipeId)
    {
        eventManager.publish(new RecipeCollectedEvent(this, recipeId));
    }

    private void sendRecipeAborted(int recipeId)
    {
        eventManager.publish(new RecipeAbortedEvent(this, recipeId));
    }

    private void sendDisableRequest(Agent agent)
    {
        eventManager.publish(new AgentDisableRequestedEvent(this, agent));
    }

    private void sendEnableRequest(Agent agent)
    {
        eventManager.publish(new AgentEnableRequestedEvent(this, agent));
    }

    private Agent createAgent(long id)
    {
        AgentConfiguration config = createAgentConfig(id);
        AgentState agentState = createAgentState(id);

        return new DefaultAgent(config, agentState, mock(AgentService.class));
    }

    private AgentConfiguration createAgentConfig(long id)
    {
        AgentConfiguration config = new AgentConfiguration();
        config.setName("agent " + id);
        config.setHandle(id + 10000);
        return config;
    }

    private AgentState createAgentState(long id)
    {
        AgentState state = new AgentState();
        state.setId(id);
        return state;
    }

    private void assertAvailableToOfflineEvents(Agent agent)
    {
        assertEvents(new AgentUnavailableEvent(this, agent), new AgentOfflineEvent(this, agent));
    }

    private void assertOfflineToAvailableEvents(Agent agent)
    {
        assertEvents(new AgentOnlineEvent(this, agent), new AgentAvailableEvent(this, agent));
    }

    private void assertEvents(Event... events)
    {
        for (Event expected: events)
        {
            assertFalse(receivedEvents.isEmpty());
            Event got = receivedEvents.remove(0);
            assertEquals(expected.getClass(), got.getClass());
            if(expected instanceof AgentEvent)
            {
                Agent expectedAgent = ((AgentEvent) expected).getAgent();
                Agent gotAgent = ((AgentEvent) got).getAgent();
                assertEquals(expectedAgent, gotAgent);
            }
            else if(expected instanceof RecipeErrorEvent)
            {
                RecipeErrorEvent expectedREE = (RecipeErrorEvent) expected;
                RecipeErrorEvent gotREE = (RecipeErrorEvent) got;
                assertEquals(expectedREE.getRecipeId(), gotREE.getRecipeId());
                assertEquals(expectedREE.getErrorMessage(), gotREE.getErrorMessage());
            }
            else
            {
                RecipeTerminateRequestEvent expectedRTR = (RecipeTerminateRequestEvent) expected;
                RecipeTerminateRequestEvent gotRTR = (RecipeTerminateRequestEvent) got;
                assertEquals(expectedRTR.getRecipeId(), gotRTR.getRecipeId());
            }
        }
    }

    private void clearEvents()
    {
        receivedEvents.clear();
    }

    private void assertNoEvents()
    {
        assertTrue(receivedEvents.isEmpty());
    }

    private void assertEnableState(Agent agent, AgentState.EnableState state)
    {
        assertFalse(enableStates.isEmpty());
        Pair<Long, AgentState.EnableState> pair = enableStates.remove(0);
        assertEquals((Long) agent.getId(), pair.first);
        assertEquals(state, pair.second);
    }

    private void assertNoMoreEnableStates()
    {
        assertTrue(enableStates.isEmpty());
    }

    private void onComplete()
    {
        assertNoEvents();
        assertNoMoreEnableStates();
    }

    public void handleEvent(Event event)
    {
        if (event.getSource() != this)
        {
            receivedEvents.add(event);
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{Event.class};
    }
}
