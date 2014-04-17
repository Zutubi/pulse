package com.zutubi.pulse.master.agent;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.zutubi.events.*;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.engine.api.BuildProperties;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.core.events.RecipeEvent;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.events.*;
import com.zutubi.pulse.master.events.build.*;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.HostState;
import com.zutubi.pulse.master.tove.config.admin.AgentPingConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.agent.PingStatus;
import com.zutubi.pulse.servercore.services.HostStatus;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.tove.variables.SimpleVariable;
import com.zutubi.util.adt.Pair;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

import static com.zutubi.pulse.master.agent.AgentStatus.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class AgentStatusManagerTest extends PulseTestCase implements EventListener
{
    private static final int DEFAULT_AGENT_ID = 1;

    private EventManager eventManager;
    private AgentStatusManager agentStatusManager;
    private AgentPingConfiguration agentPingConfiguration = new AgentPingConfiguration();
    private List<Event> receivedEvents = new LinkedList<Event>();
    private RecordingEventListener statusEventListener;
    private List<Pair<Long, AgentState.EnableState>> enableStates = new LinkedList<Pair<Long, AgentState.EnableState>>();

    protected void setUp() throws Exception
    {
        super.setUp();
        eventManager = new DefaultEventManager();
        eventManager.register(this);

        // Listen for status events separately: we don't care about the order
        // with respect to connectivity/availability events.
        statusEventListener = new RecordingEventListener(AgentStatusChangeEvent.class);
        eventManager.register(statusEventListener);

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

        ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
        stub(configurationProvider.get(AgentPingConfiguration.class)).toReturn(agentPingConfiguration);
        
        agentStatusManager = new AgentStatusManager(agentPersistentStatusManager, new Executor()
        {
            public void execute(Runnable command)
            {
                // Pump events synchronously to avoid test races.  The status
                // manager itself never handles them so it matters not to the
                // accuracy of the tests.
                command.run();
            }
        }, eventManager, configurationProvider);

        // For a little realism, create a "master" agent.
        Agent a = addAgent(0);
        sendPing(a, new HostStatus(PingStatus.IDLE));
        clearEvents();
        
        setTimeout(60);
    }

    public void testAddAgent()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);

        List<Agent> allAgents = agentStatusManager.getAllAgents();
        assertEquals(2, allAgents.size());
        assertTrue(allAgents.contains(agent));

        List<Agent> onlineAgents = getOnlineAgents();
        assertEquals(DEFAULT_AGENT_ID, onlineAgents.size());
        assertFalse(onlineAgents.contains(agent));

        onComplete();
    }

    public void testOnlineAvailableAgent()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        clearEvents();
        synchroniseCycle(agent);
        
        assertOfflineToAvailableEvents(agent);
        assertStatusChanges(agent, INITIAL, SYNCHRONISING, SYNCHRONISED, IDLE);

        List<Agent> onlineAgents = getOnlineAgents();
        assertEquals(2, onlineAgents.size());
        assertTrue(onlineAgents.contains(agent));

        onComplete();
    }

    public void testAgentOffline()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        clearEvents();
        sendPing(agent, new HostStatus(PingStatus.OFFLINE));
        assertStatusChanges(agent, INITIAL, OFFLINE);
        sendPing(agent, new HostStatus(PingStatus.OFFLINE));

        onComplete();
    }

    public void testFailedSynchronise()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        clearEvents();
        sendPing(agent, new HostStatus(PingStatus.IDLE));
        assertEvents(new AgentOnlineEvent(this, agent));

        sendSynchronisationComplete(agent, false);

        assertEvents(new AgentOfflineEvent(this, agent), new AgentPingRequestedEvent(this, agent));
        assertStatusChanges(agent, INITIAL, SYNCHRONISING, OFFLINE);

        // Ensure that a successful synchronise can follow.
        synchroniseCycle(agent);

        assertOfflineToAvailableEvents(agent);
        assertStatusChanges(agent, OFFLINE, SYNCHRONISING, SYNCHRONISED, IDLE);
        
        onComplete();
    }

    public void testOfflinePingDuringSynchronise()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        sendPing(agent, new HostStatus(PingStatus.IDLE));
        assertEquals(SYNCHRONISING, agent.getStatus());
        clearEvents();

        sendPing(agent, new HostStatus(PingStatus.OFFLINE));

        assertEquals(SYNCHRONISING, agent.getStatus());
        assertNoEvents();
        assertNoStatusChanges();

        onComplete();
    }

    public void testDisableDuringSynchronise()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        sendPing(agent, new HostStatus(PingStatus.IDLE));
        assertEquals(SYNCHRONISING, agent.getStatus());
        clearEvents();

        sendDisableRequest(agent);

        assertEquals(SYNCHRONISING, agent.getStatus());
        assertEnableState(agent, AgentState.EnableState.DISABLING);
        sendSynchronisationComplete(agent, true);

        assertEnableState(agent, AgentState.EnableState.DISABLED);

        assertEvents(new AgentOfflineEvent(this, agent));
        assertStatusChanges(agent, SYNCHRONISING, DISABLED);

        onComplete();
    }

    public void testAgentExecuteRecipe()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        sendBuilding(agent, 1000);
        sendBuilding(agent, 1000);
        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testAgentRecipeError()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        clearEvents();

        sendRecipeCollecting(agent, 1000);
        assertNoEvents();
        assertEquals(POST_RECIPE, agent.getStatus());
        collectRecipe(agent, 1000);

        onComplete();
    }

    public void testAgentExecuteRecipeNoBuildingPing()
    {
        // We should handle going directly from recipe assigned to a recipe
        // complete event.
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testAgentImmediateRecipeError()
    {
        // We should handle going directly from recipe assigned to a recipe
        // error event.
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);

        sendRecipeCollecting(agent, 1000);
        assertNoEvents();
        assertEquals(POST_RECIPE, agent.getStatus());
        collectRecipe(agent, 1000);

        onComplete();
    }

    public void testRecipeAssignedIdlePingRace()
    {
        // It is valid to see an idle ping post recipe assign, before dispatch.
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        synchroniseCycle(agent);
        clearEvents();
        sendRecipeAssigned(agent, 1000);
        clearEvents();

        sendPing(agent, new HostStatus(PingStatus.IDLE, false));
        assertNoEvents();
        sendRecipeDispatched(agent, 1000);
        sendBuilding(agent, 1000);
        assertNoEvents();
        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testRecipeDispatchIdlePingRace()
    {
        // It is valid to see an idle ping post recipe dispatch.
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        synchroniseCycle(agent);
        clearEvents();
        sendRecipeAssigned(agent, 1000);
        sendRecipeDispatched(agent, 1000);
        clearEvents();

        sendPing(agent, new HostStatus(PingStatus.IDLE, false));
        assertNoEvents();
        sendBuilding(agent, 1000);
        assertNoEvents();
        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testRecipeDispatchedNeverBuilding()
    {
        // If the agent keeps returning idle pings after the timeout post
        // recipe dispatch, then we assume it is not a race but an error.
        lowerTimeout();
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        synchroniseCycle(agent);
        clearEvents();
        sendRecipeAssigned(agent, 1000);
        sendRecipeDispatched(agent, 1000);
        clearEvents();
        waitForTimeout();
        sendPing(agent, new HostStatus(PingStatus.IDLE, false));
        assertEvents(new RecipeErrorEvent(this, 1000, "Agent idle after recipe expected to have commenced (agent: agent 1, recipe: 1000, since ping: 2, timeout: 1)", true));
        sendRecipeCollecting(agent, 1000);

        onComplete();
    }

    public void testPostRecipePings()
    {
        // Post stage actions are free to fiddle with agents in the post
        // recipe state, so no ping will have an affect.
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);

        sendRecipeCollecting(agent, 1000);
        sendBuilding(agent, 1000);
        assertNoEvents();
        assertEquals(POST_RECIPE, agent.getStatus());
        sendPing(agent, new HostStatus(PingStatus.IDLE, false));
        assertNoEvents();
        assertEquals(POST_RECIPE, agent.getStatus());
        sendPing(agent, new HostStatus(PingStatus.OFFLINE, false));
        assertNoEvents();
        assertEquals(POST_RECIPE, agent.getStatus());
        sendPing(agent, new HostStatus(PingStatus.VERSION_MISMATCH, false));
        assertNoEvents();
        assertEquals(POST_RECIPE, agent.getStatus());

        onComplete();
    }

    public void testAwaitingPingBuildingPingRace()
    {
        // Although unlikely, we could see building pings for the previous
        // recipe in the awaiting ping state.  These should be ignored as
        // they are racey.
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendRecipeCollecting(agent, 1000);
        sendRecipeCollected(agent, 1000);
        clearEvents();
        assertEquals(AWAITING_PING, agent.getStatus());
        sendBuilding(agent, 1000);
        assertNoEvents();
        assertEquals(AWAITING_PING, agent.getStatus());

        onComplete();
    }

    public void testAwaitingPingBuildingPingTimeout()
    {
        // If building pings for the previous recipe continue to arrive while
        // we are in the awaiting ping state, which should eventually timeout
        // and try and regain the agent's attention.
        lowerTimeout();
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendRecipeCollecting(agent, 1000);
        sendRecipeCollected(agent, 1000);
        clearEvents();
        assertEquals(AWAITING_PING, agent.getStatus());
        assertNoEvents();
        waitForTimeout();
        sendBuilding(agent, 1000);
        assertEquals(BUILDING_INVALID, agent.getStatus());
        assertEvents(new RecipeTerminateRequestEvent(this, null, 1000));
        assertStatusChanges(agent, AWAITING_PING, BUILDING_INVALID);
        onComplete();
    }

    public void testIntermittentOfflineDuringRecipe()
    {
        // One offline ping is not enough to kill a recipe.
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);

        sendBuilding(agent, 1000);
        sendPing(agent, new HostStatus(PingStatus.OFFLINE));
        assertNoEvents();
        sendBuilding(agent, 1000);
        assertNoEvents();
        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testOfflineDuringRecipe()
    {
        // Continued offline pings pass the timeout should kill a recipe.
        lowerTimeout();
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);

        sendBuilding(agent, 1000);
        waitForTimeout();
        sendPing(agent, new HostStatus(PingStatus.OFFLINE));

        assertFalse(receivedEvents.isEmpty());
        Event event = receivedEvents.remove(0);
        assertTrue(event instanceof RecipeErrorEvent);
        RecipeErrorEvent errorEvent = (RecipeErrorEvent) event;
        assertEquals(1000, errorEvent.getRecipeId());
        MatcherAssert.assertThat(errorEvent.getErrorMessage(), Matchers.startsWith("Connection to agent lost during recipe execution"));
        
        assertEvents(new AgentOfflineEvent(this, agent));
        assertStatusChanges(agent, BUILDING, OFFLINE);
        sendRecipeCollecting(agent, 1000);
        assertStatusChanges(agent, OFFLINE, POST_RECIPE);

        onComplete();
    }

    public void testRecipeMismatch()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);

        sendPing(agent, createBuildingStatus(2000));
        assertEvents(new RecipeErrorEvent(this, 1000, "Agent recipe mismatch", true));
        sendRecipeCollecting(agent, 1000);

        // We still go through the normal post-recipe state for recipe 1000.
        assertEquals(POST_RECIPE, agent.getStatus());
        assertEvents(new RecipeTerminateRequestEvent(this, null, 2000));

        // Assume for this test that the terminate makes the agent idle for
        // the next ping.
        collectRecipe(agent, 1000);

        onComplete();
    }

    public void testRecipeMismatchTerminateFails()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);

        sendPing(agent, createBuildingStatus(2000));
        assertEvents(new RecipeErrorEvent(this, 1000, "Agent recipe mismatch", true));
        sendRecipeCollecting(agent, 1000);

        // We still go through the normal post-recipe state for recipe 1000.
        assertEquals(POST_RECIPE, agent.getStatus());
        assertEvents(new RecipeTerminateRequestEvent(this, null, 2000));

        // What happens if the terminate failed and the wretched thing is
        // still building the wrong recipe?
        sendRecipeCollected(agent, 1000);
        assertEquals(AWAITING_PING, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));
        sendPing(agent, createBuildingStatus(2000));
        assertEquals(BUILDING_INVALID, agent.getStatus());
        assertEvents(new RecipeTerminateRequestEvent(this, null, 2000));
        assertStatusChanges(agent, AWAITING_PING, BUILDING_INVALID);

        onComplete();
    }

    public void testBuildingInvalidCompletes()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        sendBuilding(agent, 1000);
        clearEvents();
        assertEquals(BUILDING_INVALID, agent.getStatus());

        sendPing(agent, new HostStatus(PingStatus.IDLE));
        assertEvents(new AgentAvailableEvent(this, agent));
        assertStatusChanges(agent, BUILDING_INVALID, IDLE);

        onComplete();
    }

    public void testMasterBounce()
    {
        // If the master bounces, it should try to resync an agent that is
        // building a defunct recipe.
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        clearEvents();
        sendBuilding(agent, 1000);
        assertEvents(
                new RecipeTerminateRequestEvent(this, null, 1000),
                new AgentOnlineEvent(this, agent)
        );
        assertEquals(BUILDING_INVALID, agent.getStatus());
        assertStatusChanges(agent, INITIAL, BUILDING_INVALID);

        onComplete();
    }

    public void testAgentBounce()
    {
        // If an agent bounces between pings, we should act like we saw it
        // offline.
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        synchroniseCycle(agent);
        clearEvents();

        sendPing(agent, new HostStatus(PingStatus.IDLE, true));
        assertAvailableToOfflineEvents(agent);
        assertEvents(new AgentOnlineEvent(this, agent));
        sendSynchronisationComplete(agent, true);
        assertEvents(new AgentPingRequestedEvent(this, agent));
        sendPing(agent, new HostStatus(PingStatus.IDLE));
        assertEvents(new AgentAvailableEvent(this, agent));
        assertEquals(IDLE, agent.getStatus());
        assertStatusChanges(agent, IDLE, OFFLINE, SYNCHRONISING, SYNCHRONISED, IDLE);

        onComplete();
    }

    public void testDisableOfflineAgent()
    {
        addAgentAndDisable(DEFAULT_AGENT_ID);

        onComplete();
    }

    public void testPingDisabled()
    {
        Agent agent = addAgentAndDisable(DEFAULT_AGENT_ID);
        sendPing(agent, new HostStatus(PingStatus.IDLE));

        onComplete();
    }

    public void testDisableOnlineAgent()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        synchroniseCycle(agent);
        clearEvents();

        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLED);
        assertAvailableToOfflineEvents(agent);
        assertStatusChanges(agent, IDLE, DISABLED);

        onComplete();
    }

    public void testDisableRecipeAssignedAgent()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        clearEvents();

        completeDisableOnIdleBuild(agent);

        onComplete();
    }

    public void testDisableBuildingAgent()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        clearEvents();

        completeDisableOnIdleBuild(agent);

        onComplete();
    }

    private void completeDisableOnIdleBuild(Agent agent)
    {
        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLING);
        sendRecipeCollecting(agent, 1000);
        sendRecipeCollected(agent, 1000);
        assertBusyAgentDisabled(agent);
    }

    public void testDisablePostRecipeAgent()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        sendRecipeCollecting(agent, 1000);
        clearEvents();

        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLING);
        sendRecipeCollected(agent, 1000);

        assertBusyAgentDisabled(agent);

        onComplete();
    }

    public void testDisableAwaitingPingAgent()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        sendRecipeCollecting(agent, 1000);
        sendRecipeCollected(agent, 1000);
        assertEquals(AWAITING_PING, agent.getStatus());
        clearEvents();

        sendDisableRequest(agent);
        assertBusyAgentDisabled(agent);
        assertStatusChanges(agent, AWAITING_PING, DISABLED);

        onComplete();
    }

    public void testHardDisableRecipeAssignedAgent()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLING);

        hardDisableBuilding(agent);

        onComplete();
    }

    public void testHardDisableBuildingAgent()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
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
                new RecipeErrorEvent(this, 1000, "Agent disabled while recipe in progress", false)
        );
        sendRecipeCollecting(agent, 1000);
        assertEquals(POST_RECIPE, agent.getStatus());

        sendRecipeCollected(agent, 1000);
        assertBusyAgentDisabled(agent);
    }

    public void testHardDisablePostRecipeAgent()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        sendRecipeCollecting(agent, 1000);
        clearEvents();

        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLING);

        sendDisableRequest(agent);
        assertEquals(POST_RECIPE, agent.getStatus());

        sendRecipeCollected(agent, 1000);
        assertBusyAgentDisabled(agent);

        onComplete();
    }

    public void testHardDisableAtDifferentStages()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        clearEvents();
        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLING);

        sendDisableRequest(agent);
        assertEvents(
                new RecipeTerminateRequestEvent(this, null, 1000),
                new RecipeErrorEvent(this, 1000, "Agent disabled while recipe in progress", false)
        );
        sendRecipeCollecting(agent, 1000);
        assertEquals(POST_RECIPE, agent.getStatus());

        sendRecipeCollecting(agent, 1000);
        sendRecipeCollected(agent, 1000);
        assertBusyAgentDisabled(agent);

        onComplete();
    }

    public void testDisableBuildingInvalidAgent()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        sendBuilding(agent, 1000);
        clearEvents();

        assertEquals(BUILDING_INVALID, agent.getStatus());

        sendDisableRequest(agent);
        assertEquals(BUILDING_INVALID, agent.getStatus());
        assertEnableState(agent, AgentState.EnableState.DISABLING);

        sendPing(agent, new HostStatus(PingStatus.IDLE));
        assertAgentDisabled(agent);

        assertEvents(new AgentOfflineEvent(this, agent));
        assertStatusChanges(agent, BUILDING_INVALID, DISABLED);

        onComplete();
    }

    public void testEnableAgent()
    {
        Agent agent = addAgentAndDisable(DEFAULT_AGENT_ID);
        sendEnableRequest(agent);
        assertEquals(INITIAL, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));
        assertEnableState(agent, AgentState.EnableState.ENABLED);

        onComplete();
    }

    public void testEnabledAgentComesOnline()
    {
        Agent agent = addAgentAndDisable(DEFAULT_AGENT_ID);
        sendEnableRequest(agent);
        assertEquals(INITIAL, agent.getStatus());
        assertEnableState(agent, AgentState.EnableState.ENABLED);
        assertEvents(new AgentPingRequestedEvent(this, agent));

        synchroniseCycle(agent);
        assertOfflineToAvailableEvents(agent);
        assertStatusChanges(agent, INITIAL, SYNCHRONISING, SYNCHRONISED, IDLE);

        onComplete();
    }

    public void testEnableDisabling()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);

        sendDisableRequest(agent);
        assertEquals(BUILDING, agent.getStatus());
        assertEnableState(agent, AgentState.EnableState.DISABLING);
        assertNoEvents();

        sendEnableRequest(agent);
        assertEquals(BUILDING, agent.getStatus());
        assertEnableState(agent, AgentState.EnableState.ENABLED);
        assertNoEvents();

        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testRemoveOfflineAgent()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        clearEvents();
        removeAgent(agent);

        assertSlaveRemoved(agent);

        onComplete();
    }

    public void testRemoveAvailableAgent()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        synchroniseCycle(agent);
        clearEvents();
        removeAgent(agent);

        assertAvailableToOfflineEvents(agent);

        List<Agent> onlineAgents = getOnlineAgents();
        assertEquals(DEFAULT_AGENT_ID, onlineAgents.size());
        assertFalse(onlineAgents.contains(agent));

        assertSlaveRemoved(agent);

        onComplete();
    }

    public void testRemoveRecipeAssignedAgent()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        clearEvents();
        removeAgent(agent);

        removedDuringBuildHelper(agent);

        onComplete();
    }

    public void testRemoveBuildingAgent()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        clearEvents();
        removeAgent(agent);

        removedDuringBuildHelper(agent);

        onComplete();
    }

    private void removedDuringBuildHelper(Agent agent)
    {
        // The manager should kill off the recipe controller, but ignore the
        // agent as it is now meaningless to us.
        assertEvents(new RecipeErrorEvent(this, 1000, "Agent deleted while recipe in progress", false), new AgentOfflineEvent(this, agent));
        eventManager.publish(new RecipeCollectingEvent(this, 1000));

        assertSlaveRemoved(agent);
    }

    public void testRemovePostRecipeAgent()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        sendRecipeCollecting(agent, 1000);
        clearEvents();
        removeAgent(agent);

        assertEvents(new AgentOfflineEvent(this, agent));
        assertSlaveRemoved(agent);

        onComplete();
    }

    public void testRemoveAwaitingPingAgent()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        sendRecipeCollecting(agent, 1000);
        sendRecipeCollected(agent, 1000);
        clearEvents();
        removeAgent(agent);

        assertEvents(new AgentOfflineEvent(this, agent));
        assertSlaveRemoved(agent);

        onComplete();
    }

    public void testChangeOfflineAgent()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        clearEvents();
        Agent newAgent = changeAgent(agent);

        final List<Agent> allAgents = agentStatusManager.getAllAgents();
        assertFalse(allAgents.contains(agent));
        assertTrue(allAgents.contains(newAgent));

        onComplete();
    }

    public void testChangeAvailableAgent()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        sendPing(agent, new HostStatus(PingStatus.IDLE));
        clearEvents();
        Agent newAgent = changeAgent(agent);

        final List<Agent> allAgents = agentStatusManager.getAllAgents();
        assertFalse(allAgents.contains(agent));
        assertTrue(allAgents.contains(newAgent));

        onComplete();
    }

    public void testChangeBuildingAgent()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        clearEvents();
        Agent newAgent = changeAgent(agent);

        assertNoEvents();
        completeAndCollectRecipe(newAgent, 1000);

        onComplete();
    }

    public void testVersionMismatch()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        sendPing(agent, new HostStatus(PingStatus.VERSION_MISMATCH));

        assertEquals(VERSION_MISMATCH, agent.getStatus());
        assertStatusChanges(agent, INITIAL, VERSION_MISMATCH);

        onComplete();
    }

    public void testVersionMismatchAvailable()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        synchroniseCycle(agent);
        clearEvents();
        sendPing(agent, new HostStatus(PingStatus.VERSION_MISMATCH));

        assertEquals(VERSION_MISMATCH, agent.getStatus());
        assertAvailableToOfflineEvents(agent);
        assertStatusChanges(agent, IDLE, VERSION_MISMATCH);

        onComplete();
    }

    public void testVersionMismatchBuilding()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        clearEvents();
        sendPing(agent, new HostStatus(PingStatus.VERSION_MISMATCH));

        assertEquals(VERSION_MISMATCH, agent.getStatus());
        assertEvents(
                new RecipeErrorEvent(this, 1000, "Agent status changed to 'version mismatch' while recipe in progress", true),
                new AgentOfflineEvent(this, agent)
        );
        assertStatusChanges(agent, BUILDING, VERSION_MISMATCH);

        sendRecipeCollecting(agent, 1000);
        onComplete();
    }

    public void testPluginMismatch()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        sendPing(agent, new HostStatus(PingStatus.PLUGIN_MISMATCH));

        assertEquals(PLUGIN_MISMATCH, agent.getStatus());
        assertStatusChanges(agent, INITIAL, PLUGIN_MISMATCH);

        onComplete();
    }

    public void testPluginMismatchBuilding()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        clearEvents();
        sendPing(agent, new HostStatus(PingStatus.PLUGIN_MISMATCH));

        assertEquals(PLUGIN_MISMATCH, agent.getStatus());
        assertEvents(
                new RecipeErrorEvent(this, 1000, "Agent status changed to 'plugin mismatch' while recipe in progress", true),
                new AgentOfflineEvent(this, agent)
        );
        assertStatusChanges(agent, BUILDING, PLUGIN_MISMATCH);

        sendRecipeCollecting(agent, 1000);
        onComplete();
    }
    
    public void testTokenMismatch()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        sendPing(agent, new HostStatus(PingStatus.TOKEN_MISMATCH));

        assertEquals(TOKEN_MISMATCH, agent.getStatus());
        assertStatusChanges(agent, INITIAL, TOKEN_MISMATCH);

        onComplete();
    }

    public void testTokenMismatchAvailable()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        synchroniseCycle(agent);
        clearEvents();
        sendPing(agent, new HostStatus(PingStatus.TOKEN_MISMATCH));

        assertEquals(TOKEN_MISMATCH, agent.getStatus());
        assertAvailableToOfflineEvents(agent);
        assertStatusChanges(agent, IDLE, TOKEN_MISMATCH);

        onComplete();
    }

    public void testTokenMismatchBuilding()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        clearEvents();
        sendPing(agent, new HostStatus(PingStatus.TOKEN_MISMATCH));

        assertEquals(TOKEN_MISMATCH, agent.getStatus());
        assertEvents(
                new RecipeErrorEvent(this, 1000, "Agent status changed to 'token mismatch' while recipe in progress", true),
                new AgentOfflineEvent(this, agent)
        );
        assertStatusChanges(agent, BUILDING, TOKEN_MISMATCH);
        sendRecipeCollecting(agent, 1000);

        onComplete();
    }

    public void testInvalidMaster()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        sendPing(agent, new HostStatus(PingStatus.INVALID_MASTER));

        assertEquals(INVALID_MASTER, agent.getStatus());
        assertStatusChanges(agent, INITIAL, INVALID_MASTER);

        onComplete();
    }

    public void testInvalidMasterAvailable()
    {
        Agent agent = addAgent(DEFAULT_AGENT_ID);
        synchroniseCycle(agent);
        clearEvents();
        sendPing(agent, new HostStatus(PingStatus.INVALID_MASTER));

        assertEquals(INVALID_MASTER, agent.getStatus());
        assertAvailableToOfflineEvents(agent);
        assertStatusChanges(agent, IDLE, INVALID_MASTER);

        onComplete();
    }

    public void testInvalidMasterBuilding()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        clearEvents();
        sendPing(agent, new HostStatus(PingStatus.INVALID_MASTER));

        assertEquals(INVALID_MASTER, agent.getStatus());
        assertEvents(
                new RecipeErrorEvent(this, 1000, "Agent status changed to 'invalid master' while recipe in progress", true),
                new AgentOfflineEvent(this, agent)
        );
        assertStatusChanges(agent, BUILDING, INVALID_MASTER);
        sendRecipeCollecting(agent, 1000);

        onComplete();
    }

    public void testAbortAfterDispatch()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendRecipeAborted(1000);
        assertEquals(AWAITING_PING, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));
        assertStatusChanges(agent, RECIPE_DISPATCHED, AWAITING_PING);

        sendPing(agent, new HostStatus(PingStatus.IDLE));
        assertEquals(SYNCHRONISING, agent.getStatus());
        assertStatusChanges(agent, AWAITING_PING, SYNCHRONISING);

        onComplete();
    }

    public void testAbortBuilding()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        assertEquals(BUILDING, agent.getStatus());
        sendRecipeAborted(1000);
        assertEquals(AWAITING_PING, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));
        assertStatusChanges(agent, BUILDING, AWAITING_PING);

        onComplete();
    }

    public void testAbortDisabling()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendDisableRequest(agent);
        sendBuilding(agent, 1000);
        assertEnableState(agent, AgentState.EnableState.DISABLING);
        sendRecipeAborted(1000);
        assertBusyAgentDisabled(agent);
        assertStatusChanges(agent, BUILDING, DISABLED);

        onComplete();
    }

    public void testAbortCollecting()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        sendRecipeCollecting(agent, 1000);
        assertEquals(POST_RECIPE, agent.getStatus());
        sendRecipeAborted(1000);
        assertEquals(AWAITING_PING, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));
        assertStatusChanges(agent, POST_RECIPE, AWAITING_PING);

        onComplete();
    }
    
    public void testAbortIdle()
    {
        Agent agent = addAgentAndDispatchRecipe(DEFAULT_AGENT_ID, 1000);
        sendBuilding(agent, 1000);
        sendRecipeCollecting(agent, 1000);
        sendRecipeCollected(agent, 1000);
        assertEvents(new AgentPingRequestedEvent(this, agent));
        synchroniseCycle(agent);
        assertEquals(IDLE, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent), new AgentAvailableEvent(this, agent));
        assertStatusChanges(agent, AWAITING_PING, SYNCHRONISING, SYNCHRONISED, IDLE);
        sendRecipeAborted(1000);
        assertEquals(IDLE, agent.getStatus());

        onComplete();
    }

    private void assertBusyAgentDisabled(Agent agent)
    {
        assertAgentDisabled(agent);
        assertEvents(new AgentOfflineEvent(this, agent));
    }

    private void assertAgentDisabled(Agent agent)
    {
        assertEquals(DISABLED, agent.getStatus());
        assertEnableState(agent, AgentState.EnableState.DISABLED);
    }

    private void assertSlaveRemoved(Agent agent)
    {
        List<Agent> allAgents = agentStatusManager.getAllAgents();
        assertEquals(DEFAULT_AGENT_ID, allAgents.size());
        assertFalse(allAgents.contains(agent));
    }

    private Agent addAgentAndDispatchRecipe(int agentId, int recipeId)
    {
        Agent agent = addAgent(agentId);
        synchroniseCycle(agent);
        clearEvents();
        sendRecipeAssigned(agent, recipeId);
        assertEvents(new AgentUnavailableEvent(this, agent));
        assertEquals(RECIPE_ASSIGNED, agent.getStatus());
        sendRecipeDispatched(agent, recipeId);
        assertEquals(RECIPE_DISPATCHED, agent.getStatus());
        return agent;
    }

    private Agent addAgentAndDisable(int id)
    {
        Agent agent = addAgent(id);
        clearEvents();
        sendDisableRequest(agent);
        assertEnableState(agent, AgentState.EnableState.DISABLED);
        assertStatusChanges(agent, INITIAL, DISABLED);
        return agent;
    }

    private void completeAndCollectRecipe(Agent agent, int recipeId)
    {
        sendRecipeCollecting(agent, recipeId);
        assertNoEvents();
        assertEquals(POST_RECIPE, agent.getStatus());
        collectRecipe(agent, recipeId);
    }

    private void collectRecipe(Agent agent, int recipeId)
    {
        sendRecipeCollected(agent, recipeId);
        assertEquals(AWAITING_PING, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));
        assertNoEvents();
        synchroniseCycle(agent);
        assertEvents(new AgentPingRequestedEvent(this, agent), new AgentAvailableEvent(this, agent));
        assertStatusChanges(agent, AWAITING_PING, SYNCHRONISING, SYNCHRONISED, IDLE);
        assertEquals(IDLE, agent.getStatus());

        assertNoEvents();
        assertNoStatusChanges();
    }

    private void lowerTimeout()
    {
        setTimeout(DEFAULT_AGENT_ID);
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
        return agentStatusManager.getAgentsByStatusPredicate(new Predicate<AgentStatus>()
        {
            public boolean apply(AgentStatus status)
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

    private HostStatus createBuildingStatus(long recipeId)
    {
        return createBuildingStatus(DEFAULT_AGENT_ID, recipeId);
    }

    private HostStatus createBuildingStatus(long agentId, long recipeId)
    {
        return new HostStatus(ImmutableMap.of(agentIdToHandle(agentId), recipeId), false);
    }
    
    private void sendPing(Agent agent, HostStatus status)
    {
        eventManager.publish(new AgentPingEvent(this, agent, status.getStatus(), status.getRecipeId(agent.getConfig().getHandle()), status.isFirst(), status.getMessage()));
    }

    private void sendSynchronisationComplete(Agent agent, boolean successful)
    {
        eventManager.publish(new AgentSynchronisationCompleteEvent(this, agent, successful));
    }

    private void synchroniseCycle(Agent agent)
    {
        sendPing(agent, new HostStatus(PingStatus.IDLE));
        sendSynchronisationComplete(agent, true);
        sendPing(agent, new HostStatus(PingStatus.IDLE));
    }

    private void sendRecipeAssigned(Agent agent, int recipeId)
    {
        PulseExecutionContext context = new PulseExecutionContext();
        context.add(BuildProperties.NAMESPACE_INTERNAL, new SimpleVariable<String>(BuildProperties.PROPERTY_RECIPE_ID, Long.toString(recipeId)));
        eventManager.publish(new RecipeAssignedEvent(this, new RecipeRequest(context), agent));
        assertStatusChanges(agent, IDLE, RECIPE_ASSIGNED);
    }

    private void sendRecipeDispatched(Agent agent, int recipeId)
    {
        eventManager.publish(new RecipeDispatchedEvent(this, recipeId, agent));
        assertStatusChanges(agent, RECIPE_ASSIGNED, RECIPE_DISPATCHED);
    }


    private void sendBuilding(Agent agent, int recipeId)
    {
        AgentStatus statusBefore = agent.getStatus();
        sendPing(agent, createBuildingStatus(recipeId));
        if (statusBefore == RECIPE_DISPATCHED)
        {
            assertStatusChanges(agent, RECIPE_DISPATCHED, BUILDING);
        }
    }
    
    private void sendRecipeCollecting(Agent agent, int recipeId)
    {
        AgentStatus statusBefore = agent.getStatus();
        eventManager.publish(new RecipeCollectingEvent(this, recipeId));
        if (statusBefore.isBuilding())
        {
            assertStatusChanges(agent, statusBefore, POST_RECIPE);
        }
    }

    private void sendRecipeCollected(Agent agent, int recipeId)
    {
        AgentStatus newStatus = agent.isDisabling() ? DISABLED : AWAITING_PING;
        eventManager.publish(new RecipeCollectedEvent(this, recipeId));
        assertStatusChanges(agent, POST_RECIPE, newStatus);
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
        boolean wasDisabled = agent.isDisabled();
        eventManager.publish(new AgentEnableRequestedEvent(this, agent));
        if (wasDisabled)
        {
            assertStatusChanges(agent, DISABLED, INITIAL);
        }
    }

    private Agent createAgent(long id)
    {
        Host host = new DefaultHost(new HostState());

        AgentConfiguration config = createAgentConfig(id);
        AgentState agentState = createAgentState(id);

        return new DefaultAgent(config, agentState, mock(AgentService.class), host);
    }

    private AgentConfiguration createAgentConfig(long id)
    {
        AgentConfiguration config = new AgentConfiguration();
        config.setName("agent " + id);
        config.setHandle(agentIdToHandle(id));
        return config;
    }

    private long agentIdToHandle(long id)
    {
        return id + 10000;
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
        assertEvents(new AgentOnlineEvent(this, agent), new AgentPingRequestedEvent(this, agent), new AgentAvailableEvent(this, agent));
    }

    private void assertEvents(Event... events)
    {
        for (Event expected: events)
        {
            assertFalse("Expecting '" + expected + "', but got no more events", receivedEvents.isEmpty());
            Event got = receivedEvents.remove(0);
            assertEquals(expected.getClass(), got.getClass());
            if(expected instanceof AgentEvent)
            {
                Agent expectedAgent = ((AgentEvent) expected).getAgent();
                Agent gotAgent = ((AgentEvent) got).getAgent();
                assertEquals(expectedAgent, gotAgent);
            }
            else if(expected instanceof HostEvent)
            {
                Host expectedHost = ((HostEvent) expected).getHost();
                Host gotHost = ((HostEvent) got).getHost();
                assertEquals(expectedHost, gotHost);
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

    private void assertNoEvents()
    {
        assertTrue(receivedEvents.isEmpty());
    }

    private void assertStatusChanges(Agent agent, AgentStatus... statuses)
    {
        List<AgentStatusChangeEvent> expectedEvents = new LinkedList<AgentStatusChangeEvent>();
        for (int i = 0; i < statuses.length - 1; i++)
        {
            expectedEvents.add(new AgentStatusChangeEvent(this, agent, statuses[i], statuses[i + 1]));
        }

        assertEquals(expectedEvents, statusEventListener.getEventsReceived(AgentStatusChangeEvent.class));
        statusEventListener.reset();
    }

    private void assertNoStatusChanges()
    {
        assertEquals("Expected no status events, got: " + statusEventListener.getEventsReceived(), 0, statusEventListener.getReceivedCount());
    }

    private void clearEvents()
    {
        receivedEvents.clear();
        statusEventListener.reset();
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
        assertNoStatusChanges();
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
        return new Class[] {
                AgentAvailabilityEvent.class,
                AgentConnectivityEvent.class,
                AgentPingRequestedEvent.class,
                RecipeEvent.class,
                RecipeTerminateRequestEvent.class
        };
    }
}
