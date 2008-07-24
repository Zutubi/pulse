package com.zutubi.pulse.agent;

import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.build.*;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.util.Predicate;
import com.zutubi.pulse.util.logging.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages the transient status of agents.  Uses ping and recipe events to
 * decide on the agent status.
 * 
 * Essentially , then, this manager takes low-level indicators of an agent's
 * status and translates them into higher-level ones that are less tightly
 * coupled to the agent management code.
 */
public class AgentStatusManager implements EventListener
{
    public static final String PROPERTY_AGENT_OFFLINE_TIMEOUT = "pulse.agent.offline.timeout";

    private static final Logger LOG = Logger.getLogger(AgentStatusManager.class);

    private Map<Long, Agent> agentsById = new HashMap<Long, Agent>();
    private Map<Long, Agent> agentsByRecipeId = new HashMap<Long, Agent>();
    private Lock agentsLock = new ReentrantLock();
    private AgentPersistentStatusManager agentPersistentStatusManager;
    private EventManager eventManager;

    public static long getAgentOfflineTimeout()
    {
        return Long.getLong(PROPERTY_AGENT_OFFLINE_TIMEOUT, (long) (AgentPingService.getAgentPingInterval() * 4));
    }

    public AgentStatusManager(MasterAgent masterAgent, AgentPersistentStatusManager agentPersistentStatusManager, EventManager eventManager)
    {
        agentsById.put(masterAgent.getId(), masterAgent);
        this.agentPersistentStatusManager = agentPersistentStatusManager;
        this.eventManager = eventManager;
        eventManager.register(this);
    }

    public List<Agent> getAllAgents()
    {
        agentsLock.lock();
        try
        {
            return new LinkedList<Agent>(agentsById.values());
        }
        finally
        {
            agentsLock.unlock();
        }
    }
    
    public List<Agent> getAgentsByStatusPredicate(Predicate<Status> predicate)
    {
        List<Agent> result = new LinkedList<Agent>();
        agentsLock.lock();
        try
        {
            for(Agent a: agentsById.values())
            {
                if(predicate.satisfied(a.getStatus()))
                {
                    result.add(a);
                }
            }
        }
        finally
        {
            agentsLock.unlock();
        }

        return result;
    }

    private void handlePing(Agent agent, SlaveStatus pingStatus, List<Event> events)
    {
        SlaveAgent slaveAgent = (SlaveAgent) agentsById.get(agent.getId());
        if(slaveAgent == null || !slaveAgent.isEnabled() || slaveAgent.getStatus().ignorePings())
        {
            // Agent may be null if a ping was requested then the agent was
            // removed.  Similarly, it could be disabled after a ping
            // request.
            return;
        }

        checkForAgentBounce(slaveAgent, pingStatus, events);

        Status oldStatus = slaveAgent.getStatus();

        switch(pingStatus.getStatus())
        {
            case BUILDING:
                handlePingBuilding(slaveAgent, pingStatus, events);
                break;
            case IDLE:
                handlePingIdle(slaveAgent, pingStatus, events);
                break;
            case OFFLINE:
                handlePingOffline(slaveAgent, pingStatus, events);
                break;
            case INVALID_MASTER:
            case TOKEN_MISMATCH:
            case VERSION_MISMATCH:
                switch(oldStatus)
                {
                    case BUILDING:
                    case RECIPE_DISPATCHED:
                        events.add(new RecipeErrorEvent(this, slaveAgent.getRecipeId(), "Agent status changed to '" + pingStatus.getStatus().getPrettyString() + "' while recipe in progress"));

                        // So severe that we will not do the usual post
                        // recipe jazz.  This agent is gone proper.
                        agentsByRecipeId.remove(slaveAgent.getRecipeId());
                        slaveAgent.updateStatus(pingStatus);
                        break;

                    default:
                        slaveAgent.updateStatus(pingStatus);
                        break;
                }
                break;
        }

        // Catch-all for disable-on-idle.
        if(slaveAgent.isDisabling() && !slaveAgent.getStatus().isBusy())
        {
            agentPersistentStatusManager.setEnableState(slaveAgent, Slave.EnableState.DISABLED);
            slaveAgent.updateStatus(Status.DISABLED);
        }

        if(slaveAgent.getStatus().isOnline())
        {
            // It is important that the agent online event comes before the
            // available event as the online event may trigger some extra
            // updates to the agent (e.g. resource discovery).
            if(!oldStatus.isOnline())
            {
                events.add(new AgentOnlineEvent(this, slaveAgent));
            }

            if(slaveAgent.getStatus() == Status.IDLE && oldStatus != Status.IDLE)
            {
                events.add(new AgentAvailableEvent(this, slaveAgent));
            }
        }
        else
        {
            if(oldStatus.isOnline())
            {
                if(slaveAgent.getStatus() != Status.IDLE && oldStatus == Status.IDLE)
                {
                    events.add(new AgentUnavailableEvent(this, slaveAgent));
                }

                events.add(new AgentOfflineEvent(this, slaveAgent));
            }

            if(pingStatus.getStatus() == PingStatus.VERSION_MISMATCH)
            {
                events.add(new AgentUpgradeRequiredEvent(this, slaveAgent));
            }
        }
    }

    private void checkForAgentBounce(SlaveAgent agent, SlaveStatus pingStatus, List<Event> events)
    {
        if(agent.getStatus().isOnline() && pingStatus.isFirst())
        {
            // The agent must have bounced between pings.  Simulate the
            // master seeing this by sending an offline ping.
            handlePing(agent, new SlaveStatus(PingStatus.OFFLINE), events);
        }
    }

    private void handlePingBuilding(SlaveAgent agent, SlaveStatus pingStatus, List<Event> events)
    {
        long pingRecipe = pingStatus.getRecipeId();
        switch (agent.getStatus())
        {
            case RECIPE_DISPATCHED:
            case BUILDING:
                if (pingRecipe == agent.getRecipeId())
                {
                    // Expected case.
                    agent.updateStatus(pingStatus);
                }
                else
                {
                    // Unusual case: leave the state as-is.  We need to go
                    // through the normal post-recipe state transitions for
                    // the expected recipe (triggered by this error event).
                    // Once that is done we the awaiting ping state will move
                    // as the appropriate next status.
                    events.add(new RecipeErrorEvent(this, agent.getRecipeId(), "Agent recipe mismatch"));
                    terminateRecipe(agent, pingStatus.getRecipeId());
                }
                break;

            case AWAITING_PING:
                // If the same recipe id this could just be a race, ignore
                // such pings up until the timeout.
                if (pingRecipe != agent.getRecipeId() || agent.getSecondsSincePing() > getAgentOfflineTimeout())
                {
                    terminateRecipe(agent, pingRecipe);
                    agent.updateStatus(Status.BUILDING_INVALID, pingRecipe);
                }
                break;

            case BUILDING_INVALID:
                // Terminate it again.
                terminateRecipe(agent, pingRecipe);
                break;

            default:
                terminateRecipe(agent, pingRecipe);
                agent.updateStatus(Status.BUILDING_INVALID, pingRecipe);
                break;
        }
    }

    private void handlePingIdle(SlaveAgent agent, SlaveStatus pingStatus, List<Event> events)
    {
        switch (agent.getStatus())
        {
            case BUILDING:
                // Can happen legitimately, but if we pass the
                // timeout then presume something is wrong.
                if (agent.getSecondsSincePing() > getAgentOfflineTimeout())
                {
                    events.add(new RecipeErrorEvent(this, agent.getRecipeId(), "Agent idle before recipe expected to complete"));
                }
                break;

            case BUILDING_INVALID:
                agent.updateStatus(pingStatus);
                break;

            case RECIPE_DISPATCHED:
                if (agent.getSecondsSincePing() > getAgentOfflineTimeout())
                {
                    events.add(new RecipeErrorEvent(this, agent.getRecipeId(), "Agent idle after recipe expected to have commenced"));
                }
                break;

            default:
                agent.updateStatus(pingStatus);
                break;
        }
    }

    private void handlePingOffline(SlaveAgent agent, SlaveStatus pingStatus, List<Event> events)
    {
        switch (agent.getStatus())
        {
            case BUILDING:
            case RECIPE_DISPATCHED:
                // Don't immediately give up - wait for the timeout.
                if (agent.getSecondsSincePing() > getAgentOfflineTimeout())
                {
                    events.add(new RecipeErrorEvent(this, agent.getRecipeId(), "Connection to agent lost during recipe execution"));
                    agent.updateStatus(pingStatus);
                }
                break;

            default:
                agent.updateStatus(pingStatus);
                break;
        }
    }

    private void terminateRecipe(Agent agent, long recipeId)
    {
        try
        {
            agent.getBuildService().terminateRecipe(recipeId);
        }
        catch (Exception e)
        {
            LOG.severe("Unable to terminate unwanted recipe on agent '" + agent.getName() + "': " + e.getMessage(), e);
        }
    }

    private void handleRecipeStarted(RecipeDispatchedEvent event, List<Event> events)
    {
        Agent agent = agentsById.get(event.getAgent().getId());
        if(agent != null)
        {
            agentsByRecipeId.put(event.getRecipeId(), agent);
            events.add(new AgentUnavailableEvent(this, agent));
            if(agent.isSlave())
            {
                SlaveAgent slaveAgent = ((SlaveAgent) agent);
                slaveAgent.updateStatus(Status.RECIPE_DISPATCHED, event.getRecipeId());
            }
            else
            {
                agent.updateStatus(Status.BUILDING);
            }
        }
    }

    private void handleRecipeFinished(RecipeEvent event)
    {
        Agent agent = agentsByRecipeId.get(event.getRecipeId());
        if(agent != null)
        {
            if(agent.isSlave())
            {
                SlaveAgent slaveAgent = ((SlaveAgent) agent);
                slaveAgent.updateStatus(Status.POST_RECIPE, event.getRecipeId());
            }
            else
            {
                agent.updateStatus(Status.POST_RECIPE);
            }
        }
    }

    private void handleRecipeCollected(RecipeCollectedEvent event, List<Event> events)
    {
        Agent agent = agentsByRecipeId.remove(event.getRecipeId());
        if(agent != null)
        {
            if (agent.isDisabling())
            {
                agent.updateStatus(Status.DISABLED);
                agentPersistentStatusManager.setEnableState(agent, Slave.EnableState.DISABLED);
                events.add(new AgentOfflineEvent(this, agent));
            }
            else
            {
                if (agent.isSlave())
                {
                    SlaveAgent slaveAgent = ((SlaveAgent) agent);
                    slaveAgent.updateStatus(Status.AWAITING_PING, event.getRecipeId());

                    // Request a ping immediately so no time is wasted
                    events.add(new AgentPingRequestedEvent(this, slaveAgent));
                }
                else
                {
                    agent.updateStatus(Status.IDLE);
                    events.add(new AgentAvailableEvent(this, agent));
                }
            }
        }
    }

    private void handleDisableRequested(Agent agent, List<Event> events)
    {
        Status status = agent.getStatus();
        if (status == Status.AWAITING_PING)
        {
            // Small optimisation: no need to wait anymore.
            disableAgent(agent, events);
        }
        else
        {
            switch (agent.getEnableState())
            {
                case ENABLED:
                    if (status.isBusy())
                    {
                        // Disable on idle.
                        agentPersistentStatusManager.setEnableState(agent, Slave.EnableState.DISABLING);
                    }
                    else
                    {
                        // Immediate disable
                        if (agent.isOnline())
                        {
                            events.add(new AgentUnavailableEvent(this, agent));
                        }

                        disableAgent(agent, events);
                    }
                    break;

                case DISABLING:
                    // Hard disable.  Raising error event will lead to us
                    // disabling the agent in our handler.
                    switch (status)
                    {
                        case BUILDING:
                        case RECIPE_DISPATCHED:
                            terminateRecipe(agent, agent.getRecipeId());
                            events.add(new RecipeErrorEvent(this, agent.getRecipeId(), "Agent disabled while recipe in progress"));
                            break;
                   }
                    break;
            }
        }
    }

    private void disableAgent(Agent agent, List<Event> events)
    {
        if (agent.isOnline())
        {
            events.add(new AgentOfflineEvent(this, agent));
        }
        
        agent.updateStatus(Status.DISABLED);
        agentPersistentStatusManager.setEnableState(agent, Slave.EnableState.DISABLED);
    }

    private void handleEnableRequested(Agent agent, List<Event> events)
    {
        switch(agent.getEnableState())
        {
            case DISABLED:
            case DISABLING:
            case FAILED_UPGRADE:
                if (agent.isSlave())
                {
                    agent.updateStatus(Status.INITIAL);
                }
                else
                {
                    events.add(new AgentOnlineEvent(this, agent));
                    events.add(new AgentAvailableEvent(this, agent));
                    agent.updateStatus(Status.IDLE);
                }
                agentPersistentStatusManager.setEnableState(agent, Slave.EnableState.ENABLED);
                break;
        }
    }

    private void handleAgentAdded(SlaveAgent slaveAgent)
    {
        agentsById.put(slaveAgent.getId(), slaveAgent);
    }

    private void handleAgentRemoved(SlaveAgent slaveAgent, List<Event> events)
    {
        agentsById.remove(slaveAgent.getId());

        // If the agent is running a recipe remove it, and try to terminate
        // the recipe controller with an error if we are not too late.
        long recipeId = findRecipeId(slaveAgent);
        if(recipeId > 0)
        {
            Agent agent = agentsByRecipeId.remove(recipeId);
            switch(agent.getStatus())
            {
                case RECIPE_DISPATCHED:
                case BUILDING:
                    events.add(new RecipeErrorEvent(this, recipeId, "Agent deleted while recipe in progress"));
                    break;
            }
        }

        Status status = slaveAgent.getStatus();
        if(status.isOnline())
        {
            if (!status.isBusy())
            {
                events.add(new AgentUnavailableEvent(this, slaveAgent));
            }
            
            events.add(new AgentOfflineEvent(this, slaveAgent));
        }
    }

    private void handleAgentChanged(SlaveAgent slaveAgent)
    {
        SlaveAgent existingAgent = (SlaveAgent) agentsById.get(slaveAgent.getId());
        if(existingAgent != null)
        {
            slaveAgent.copyStatus(existingAgent);
        }

        agentsById.put(slaveAgent.getId(), slaveAgent);

        long recipeId = findRecipeId(slaveAgent);
        if(recipeId > 0)
        {
            agentsByRecipeId.put(recipeId, slaveAgent);
        }
    }

    private long findRecipeId(SlaveAgent slaveAgent)
    {
        long recipeId = 0;
        for(Map.Entry<Long, Agent> entry: agentsByRecipeId.entrySet())
        {
            if(entry.getValue().getId() == slaveAgent.getId())
            {
                recipeId = entry.getKey();
            }
        }
        return recipeId;
    }

    public void handleEvent(Event event)
    {
        List<Event> events = new LinkedList<Event>();
        agentsLock.lock();
        try
        {
            if(event instanceof AgentPingEvent)
            {
                AgentPingEvent ape = (AgentPingEvent) event;
                handlePing(ape.getAgent(), ape.getPingStatus(), events);
            }
            else if(event instanceof RecipeDispatchedEvent)
            {
                handleRecipeStarted((RecipeDispatchedEvent) event, events);
            }
            else if(event instanceof RecipeCompletedEvent || event instanceof RecipeErrorEvent)
            {
                handleRecipeFinished((RecipeEvent) event);
            }
            else if(event instanceof RecipeCollectedEvent)
            {
                handleRecipeCollected((RecipeCollectedEvent )event, events);
            }
            else if(event instanceof AgentDisableRequestedEvent)
            {
                handleDisableRequested(((AgentEvent) event).getAgent(), events);
            }
            else if(event instanceof AgentEnableRequestedEvent)
            {
                handleEnableRequested(((AgentEvent) event).getAgent(), events);
            }
            else if(event instanceof AgentAddedEvent)
            {
                handleAgentAdded(((AgentAddedEvent) event).getSlaveAgent());
            }
            else if(event instanceof AgentChangedEvent)
            {
                handleAgentChanged(((AgentChangedEvent) event).getSlaveAgent());
            }
            else if(event instanceof AgentRemovedEvent)
            {
                handleAgentRemoved(((AgentRemovedEvent) event).getSlaveAgent(), events);
            }
        }
        finally
        {
            agentsLock.unlock();
        }

        for(Event e: events)
        {
            eventManager.publish(e);
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{
                AgentAddedEvent.class,
                AgentChangedEvent.class,
                AgentDisableRequestedEvent.class,
                AgentEnableRequestedEvent.class,
                AgentPingEvent.class,
                AgentRemovedEvent.class,
                RecipeCollectedEvent.class,
                RecipeCompletedEvent.class,
                RecipeDispatchedEvent.class,
                RecipeErrorEvent.class,
        };
    }
}
