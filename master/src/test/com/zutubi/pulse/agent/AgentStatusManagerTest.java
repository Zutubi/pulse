package com.zutubi.pulse.agent;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.MasterBuildService;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.MasterConfigurationSupport;
import com.zutubi.pulse.config.PropertiesConfig;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.build.*;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.Pair;
import com.zutubi.pulse.util.Predicate;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 */
public class AgentStatusManagerTest extends PulseTestCase implements EventListener
{
    private MasterAgent masterAgent;
    private EventManager eventManager;
    private AgentStatusManager agentStatusManager;
    private List<Event> receivedEvents = new LinkedList<Event>();
    private List<Pair<Long, Slave.EnableState>> enableStates = new LinkedList<Pair<Long, Slave.EnableState>>();

    protected void setUp() throws Exception
    {
        super.setUp();
        eventManager = new DefaultEventManager();
        eventManager.register(this);

        final MasterConfigurationSupport masterConfig = new MasterConfigurationSupport(new PropertiesConfig(new Properties()));

        AgentPersistentStatusManager agentPersistentStatusManager = new AgentPersistentStatusManager()
        {
            public void setEnableState(Agent agent, Slave.EnableState state)
            {
                enableStates.add(new Pair<Long, Slave.EnableState>(agent.getId(), state));
                if(agent.isSlave())
                {
                    SlaveAgent sa = (SlaveAgent) agent;
                    Slave slave = createSlave(agent.getId());
                    slave.setEnableState(state);
                    sa.setSlave(slave);
                }
                else
                {
                    masterConfig.setMasterEnableState(state);
                }
            }
        };

        MasterConfigurationManager mockMCM = mock(MasterConfigurationManager.class);
        MasterBuildService mocmMBS = mock(MasterBuildService.class);

        stub(mockMCM.getAppConfig()).toReturn(masterConfig);
        masterAgent = new MasterAgent(mocmMBS, mockMCM, null, null);
        agentStatusManager = new AgentStatusManager(masterAgent, agentPersistentStatusManager, new Executor()
        {
            public void execute(Runnable command)
            {
                // Pump events synchronously to avoid test races.  The status
                // manager itself never handles them so it matters not to the
                // accuracy of the tests.
                command.run();
            }
        }, eventManager);

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
        SlaveAgent agent = addAgent(1);

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
        SlaveAgent agent = addAgent(1);
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
        SlaveAgent agent = addAgent(1);
        clearEvents();
        sendPing(agent, new SlaveStatus(PingStatus.OFFLINE));
        sendPing(agent, new SlaveStatus(PingStatus.OFFLINE));

        onComplete();
    }

    public void testAgentExecuteRecipe()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testMasterExecuteRecipe()
    {
        sendRecipeDispatched(masterAgent, 1000);
        assertEvents(new AgentUnavailableEvent(this, masterAgent));
        assertEquals(Status.BUILDING, masterAgent.getStatus());
        sendRecipeCollecting(1000);
        assertEquals(Status.POST_RECIPE, masterAgent.getStatus());
        sendRecipeCollected(1000);
        assertEquals(Status.IDLE, masterAgent.getStatus());
        assertEvents(new AgentAvailableEvent(this, masterAgent));

        onComplete();
    }

    public void testAgentRecipeError()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();

        sendRecipeCollecting(1000);
        assertNoEvents();
        assertEquals(Status.POST_RECIPE, agent.getStatus());
        collectRecipe(agent, 1000);

        onComplete();
    }

    public void testMasterRecipeError()
    {
        sendRecipeDispatched(masterAgent, 1000);
        assertEvents(new AgentUnavailableEvent(this, masterAgent));
        assertEquals(Status.BUILDING, masterAgent.getStatus());
        sendRecipeCollecting(1000);
        assertEquals(Status.POST_RECIPE, masterAgent.getStatus());
        sendRecipeCollected(1000);
        assertEquals(Status.IDLE, masterAgent.getStatus());
        assertEvents(new AgentAvailableEvent(this, masterAgent));

        onComplete();
    }

    public void testAgentExecuteRecipeNoBuildingPing()
    {
        // We should handle going directly from recipe dispatched to a recipe
        // complete event.
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testAgentImmediateRecipeError()
    {
        // We should handle going directly from recipe dispatched to a recipe
        // error event.
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);

        sendRecipeCollecting(1000);
        assertNoEvents();
        assertEquals(Status.POST_RECIPE, agent.getStatus());
        collectRecipe(agent, 1000);

        onComplete();
    }

    public void testRecipeDispatchIdlePingRace()
    {
        // It is valid to see an idle ping post recipe dispatch.
        SlaveAgent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        sendRecipeDispatched(agent, 1000);
        clearEvents();

        sendPing(agent, new SlaveStatus(PingStatus.IDLE, 1000, false));
        assertNoEvents();
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        assertNoEvents();
        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testRecipeDispatchNeverBuilding()
    {
        // If the agent keeps returning idle pings after the timeout post
        // recipe dispatch, then we assume it is not a race but an error.
        lowerTimeout();
        SlaveAgent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        sendRecipeDispatched(agent, 1000);
        clearEvents();
        waitForTimeout();
        sendPing(agent, new SlaveStatus(PingStatus.IDLE, 1000, false));
        assertEvents(new RecipeErrorEvent(this, 1000, "Agent idle after recipe expected to have commenced"));
        sendRecipeCollecting(1000);

        onComplete();
    }

    public void testPostRecipePings()
    {
        // Post stage actions are free to fiddle with agents in the post
        // recipe state, so no ping will have an affect.
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);

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
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
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
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
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
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);

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
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);

        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        waitForTimeout();
        sendPing(agent, new SlaveStatus(PingStatus.OFFLINE));
        assertEvents(
                new RecipeErrorEvent(this, 1000, "Connection to agent lost during recipe execution"),
                new AgentOfflineEvent(this, agent)
        );
        sendRecipeCollecting(1000);

        onComplete();
    }

    public void testRecipeMismatch()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);

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
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);

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
        SlaveAgent agent = addAgent(1);
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
        SlaveAgent agent = addAgent(1);
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
        SlaveAgent agent = addAgent(1);
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
        SlaveAgent agent = addAgentAndDisable(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));

        onComplete();
    }

    public void testDisableOnlineAgent()
    {
        SlaveAgent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        clearEvents();

        sendDisableRequest(agent);
        assertEnableState(agent, Slave.EnableState.DISABLED);
        assertAvailableToOfflineEvents(agent);

        onComplete();
    }

    public void testDisableRecipeDispatchedAgent()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        clearEvents();

        completeDisableOnIdleBuild(agent);

        onComplete();
    }

    public void testDisableBuildingAgent()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();

        completeDisableOnIdleBuild(agent);

        onComplete();
    }

    private void completeDisableOnIdleBuild(SlaveAgent agent)
    {
        sendDisableRequest(agent);
        assertEnableState(agent, Slave.EnableState.DISABLING);
        sendRecipeCollecting(1000);
        sendRecipeCollected(1000);
        assertBusyAgentDisabled(agent);
    }

    public void testDisablePostRecipeAgent()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendRecipeCollecting(1000);
        clearEvents();

        sendDisableRequest(agent);
        assertEnableState(agent, Slave.EnableState.DISABLING);
        sendRecipeCollected(1000);

        assertBusyAgentDisabled(agent);

        onComplete();
    }

    public void testDisableAwaitingPingAgent()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendRecipeCollecting(1000);
        sendRecipeCollected(1000);
        assertEquals(Status.AWAITING_PING, agent.getStatus());
        clearEvents();

        sendDisableRequest(agent);
        assertBusyAgentDisabled(agent);

        onComplete();
    }

    public void testDisableMaster()
    {
        sendDisableRequest(masterAgent);
        assertAgentDisabled(masterAgent);
        assertAvailableToOfflineEvents(masterAgent);

        onComplete();
    }

    public void testDisableMasterBuilding()
    {
        sendRecipeDispatched(masterAgent, 1000);
        clearEvents();
        
        sendDisableRequest(masterAgent);

        assertEnableState(masterAgent, Slave.EnableState.DISABLING);

        sendRecipeCollecting(1000);
        assertEquals(Status.POST_RECIPE, masterAgent.getStatus());
        sendRecipeCollected(1000);
        assertBusyAgentDisabled(masterAgent);

        onComplete();
    }

    public void testHardDisableRecipeDispatchAgent()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendDisableRequest(agent);
        assertEnableState(agent, Slave.EnableState.DISABLING);

        hardDisableBuilding(agent);

        onComplete();
    }

    public void testHardDisableBuildingAgent()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();

        sendDisableRequest(agent);
        assertEnableState(agent, Slave.EnableState.DISABLING);

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
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        sendRecipeCollecting(1000);
        clearEvents();

        sendDisableRequest(agent);
        assertEnableState(agent, Slave.EnableState.DISABLING);

        sendDisableRequest(agent);
        assertEquals(Status.POST_RECIPE, agent.getStatus());

        sendRecipeCollected(1000);
        assertBusyAgentDisabled(agent);

        onComplete();
    }

    public void testHardDisableAtDifferentStages()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();
        sendDisableRequest(agent);
        assertEnableState(agent, Slave.EnableState.DISABLING);

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
        SlaveAgent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();

        assertEquals(Status.BUILDING_INVALID, agent.getStatus());

        sendDisableRequest(agent);
        assertEquals(Status.BUILDING_INVALID, agent.getStatus());
        assertEnableState(agent, Slave.EnableState.DISABLING);

        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        assertAgentDisabled(agent);

        assertEvents(new AgentOfflineEvent(this, agent));

        onComplete();
    }

    public void testEnableAgent()
    {
        SlaveAgent agent = addAgentAndDisable(1);
        sendEnableRequest(agent);
        assertEquals(Status.INITIAL, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));
        assertEnableState(agent, Slave.EnableState.ENABLED);

        onComplete();
    }

    public void testEnableMaster()
    {
        sendDisableRequest(masterAgent);
        assertAgentDisabled(masterAgent);
        clearEvents();

        sendEnableRequest(masterAgent);
        assertEquals(Status.IDLE, masterAgent.getStatus());
        assertEnableState(masterAgent, Slave.EnableState.ENABLED);

        assertOfflineToAvailableEvents(masterAgent);

        onComplete();
    }

    public void testEnabledAgentComesOnline()
    {
        SlaveAgent agent = addAgentAndDisable(1);
        sendEnableRequest(agent);
        assertEquals(Status.INITIAL, agent.getStatus());
        assertEnableState(agent, Slave.EnableState.ENABLED);
        assertEvents(new AgentPingRequestedEvent(this, agent));

        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        assertOfflineToAvailableEvents(agent);

        onComplete();
    }

    public void testEnableDisabling()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));

        sendDisableRequest(agent);
        assertEquals(Status.BUILDING, agent.getStatus());
        assertEnableState(agent, Slave.EnableState.DISABLING);
        assertNoEvents();

        sendEnableRequest(agent);
        assertEquals(Status.BUILDING, agent.getStatus());
        assertEnableState(agent, Slave.EnableState.ENABLED);
        assertNoEvents();

        completeAndCollectRecipe(agent, 1000);

        onComplete();
    }

    public void testEnableDisablingMaster()
    {
        sendRecipeDispatched(masterAgent, 1000);
        clearEvents();

        sendDisableRequest(masterAgent);
        assertEnableState(masterAgent, Slave.EnableState.DISABLING);

        sendEnableRequest(masterAgent);
        assertEquals(Status.BUILDING,  masterAgent.getStatus());
        assertEnableState(masterAgent, Slave.EnableState.ENABLED);
        assertNoEvents();

        sendRecipeCollecting(1000);
        assertEquals(Status.POST_RECIPE, masterAgent.getStatus());
        sendRecipeCollected(1000);
        assertEvents(new AgentAvailableEvent(this, masterAgent));

        onComplete();
    }

    public void testRemoveOfflineAgent()
    {
        SlaveAgent agent = addAgent(1);
        clearEvents();
        removeAgent(agent);

        assertSlaveRemoved(agent);

        onComplete();
    }

    public void testRemoveAvailableAgent()
    {
        SlaveAgent agent = addAgent(1);
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

    public void testRemoveRecipeDispatchedAgent()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        clearEvents();
        removeAgent(agent);

        removedDuringBuildHelper(agent);

        onComplete();
    }

    public void testRemoveBuildingAgent()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();
        removeAgent(agent);

        removedDuringBuildHelper(agent);

        onComplete();
    }

    private void removedDuringBuildHelper(SlaveAgent agent)
    {
        // The manager should kill off the recipe controller, but ignore the
        // agent as it is now meaningless to us.
        assertEvents(new RecipeErrorEvent(this, 1000, "Agent deleted while recipe in progress"), new AgentOfflineEvent(this, agent));
        sendRecipeCollecting(1000);

        assertSlaveRemoved(agent);
    }

    public void testRemovePostRecipeAgent()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
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
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
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
        SlaveAgent agent = addAgent(1);
        clearEvents();
        SlaveAgent newAgent = changeAgent(agent);

        final List<Agent> allAgents = agentStatusManager.getAllAgents();
        assertFalse(allAgents.contains(agent));
        assertTrue(allAgents.contains(newAgent));

        onComplete();
    }

    public void testChangeAvailableAgent()
    {
        SlaveAgent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        clearEvents();
        SlaveAgent newAgent = changeAgent(agent);

        final List<Agent> allAgents = agentStatusManager.getAllAgents();
        assertFalse(allAgents.contains(agent));
        assertTrue(allAgents.contains(newAgent));

        onComplete();
    }

    public void testChangeBuildingAgent()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        clearEvents();
        SlaveAgent newAgent = changeAgent(agent);

        assertNoEvents();
        completeAndCollectRecipe(newAgent, 1000);

        onComplete();
    }

    public void testVersionMismatch()
    {
        SlaveAgent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.VERSION_MISMATCH));

        assertEquals(Status.VERSION_MISMATCH, agent.getStatus());
        assertEvents(new AgentUpgradeRequiredEvent(this, agent));

        onComplete();
    }

    public void testVersionMismatchAvailable()
    {
        SlaveAgent agent = addAgent(1);
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
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
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
        SlaveAgent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.TOKEN_MISMATCH));

        assertEquals(Status.TOKEN_MISMATCH, agent.getStatus());

        onComplete();
    }

    public void testTokenMismatchAvailable()
    {
        SlaveAgent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        clearEvents();
        sendPing(agent, new SlaveStatus(PingStatus.TOKEN_MISMATCH));

        assertEquals(Status.TOKEN_MISMATCH, agent.getStatus());
        assertAvailableToOfflineEvents(agent);

        onComplete();
    }

    public void testTokenMismatchBuilding()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
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
        SlaveAgent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.INVALID_MASTER));

        assertEquals(Status.INVALID_MASTER, agent.getStatus());

        onComplete();
    }

    public void testInvalidMasterAvailable()
    {
        SlaveAgent agent = addAgent(1);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        clearEvents();
        sendPing(agent, new SlaveStatus(PingStatus.INVALID_MASTER));

        assertEquals(Status.INVALID_MASTER, agent.getStatus());
        assertAvailableToOfflineEvents(agent);

        onComplete();
    }

    public void testInvalidMasterBuilding()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
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

    public void testAbortAfterDispatch()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
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
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        assertEquals(Status.BUILDING, agent.getStatus());
        sendRecipeAborted(1000);
        assertEquals(Status.AWAITING_PING, agent.getStatus());
        assertEvents(new AgentPingRequestedEvent(this, agent));

        onComplete();
    }

    public void testAbortDisabling()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
        sendDisableRequest(agent);
        sendPing(agent, new SlaveStatus(PingStatus.BUILDING, 1000, false));
        assertEnableState(agent, Slave.EnableState.DISABLING);
        sendRecipeAborted(1000);
        assertBusyAgentDisabled(agent);

        onComplete();
    }

    public void testAbortCollecting()
    {
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
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
        SlaveAgent agent = addAgentAndDispatchRecipe(1, 1000);
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

    public void testAbortMasterBuilding()
    {
        sendRecipeDispatched(masterAgent, 1000);
        clearEvents();
        assertEquals(Status.BUILDING, masterAgent.getStatus());
        sendRecipeAborted(1000);
        assertEquals(Status.IDLE, masterAgent.getStatus());
        assertEvents(new AgentAvailableEvent(this, masterAgent));

        onComplete();
    }
    
    public void testAbortMasterDisabling()
    {
        sendRecipeDispatched(masterAgent, 1000);
        clearEvents();
        sendDisableRequest(masterAgent);
        assertEnableState(masterAgent, Slave.EnableState.DISABLING);
        sendRecipeAborted(1000);
        assertBusyAgentDisabled(masterAgent);

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
        assertEnableState(agent, Slave.EnableState.DISABLED);
    }

    private void assertSlaveRemoved(SlaveAgent agent)
    {
        List<Agent> allAgents = agentStatusManager.getAllAgents();
        assertEquals(1, allAgents.size());
        assertFalse(allAgents.contains(agent));
    }

    private SlaveAgent addAgentAndDispatchRecipe(int agentId, int recipeId)
    {
        SlaveAgent agent = addAgent(agentId);
        sendPing(agent, new SlaveStatus(PingStatus.IDLE));
        clearEvents();
        sendRecipeDispatched(agent, recipeId);
        assertEvents(new AgentUnavailableEvent(this, agent));
        assertEquals(Status.RECIPE_DISPATCHED, agent.getStatus());
        return agent;
    }

    private SlaveAgent addAgentAndDisable(int id)
    {
        SlaveAgent agent = addAgent(id);
        clearEvents();
        sendDisableRequest(agent);
        assertEnableState(agent, Slave.EnableState.DISABLED);
        return agent;
    }

    private void completeAndCollectRecipe(SlaveAgent agent, int recipeId)
    {
        sendRecipeCollecting(recipeId);
        assertNoEvents();
        assertEquals(Status.POST_RECIPE, agent.getStatus());
        collectRecipe(agent, recipeId);
    }

    private void collectRecipe(SlaveAgent agent, int recipeId)
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

    private void setTimeout(long seconds)
    {
        System.setProperty(AgentStatusManager.PROPERTY_AGENT_OFFLINE_TIMEOUT, Long.toString(seconds));
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

    private SlaveAgent addAgent(int id)
    {
        SlaveAgent agent = createSlaveAgent(id);
        eventManager.publish(new AgentAddedEvent(this, agent));
        return agent;
    }

    private void removeAgent(SlaveAgent agent)
    {
        eventManager.publish(new AgentRemovedEvent(this, agent));
    }

    private SlaveAgent changeAgent(SlaveAgent agent)
    {
        agent = createSlaveAgent(agent.getId());
        eventManager.publish(new AgentChangedEvent(this, agent));
        return agent;
    }

    private void sendPing(SlaveAgent agent, SlaveStatus status)
    {
        eventManager.publish(new AgentPingEvent(this, agent, status));
    }

    private void sendRecipeDispatched(Agent agent, int recipeId)
    {
        eventManager.publish(new RecipeDispatchedEvent(this, new RecipeRequest(recipeId, null, null, null), agent));
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

    private SlaveAgent createSlaveAgent(long id)
    {
        return new SlaveAgent(createSlave(id), null, null, mock(BuildService.class));
    }

    private Slave createSlave(long id)
    {
        Slave slave = new Slave("slave " + id, "localhost");
        slave.setId(id);
        return slave;
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

    private void assertEnableState(Agent agent, Slave.EnableState state)
    {
        assertFalse(enableStates.isEmpty());
        Pair<Long, Slave.EnableState> pair = enableStates.remove(0);
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
