package com.zutubi.pulse.agent;

import com.zutubi.pulse.events.*;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.build.*;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.services.SlaveStatus;
import com.zutubi.pulse.util.Predicate;
import com.zutubi.pulse.util.logging.Logger;

import java.util.*;
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

    private MasterAgent masterAgent;
    private Map<Long, Agent> agentsById = new HashMap<Long, Agent>();
    private Map<Long, Agent> agentsByRecipeId = new HashMap<Long, Agent>();
    private Lock agentsLock = new ReentrantLock();
    private EventManager eventManager;

    public static long getAgentOfflineTimeout()
    {
        return Long.getLong(PROPERTY_AGENT_OFFLINE_TIMEOUT, (long) (AgentPingService.getAgentPingInterval() * 4));
    }

    public AgentStatusManager(MasterAgent masterAgent, EventManager eventManager)
    {
        this.masterAgent = masterAgent;
        agentsById.put(masterAgent.getId(), masterAgent);
        this.eventManager = eventManager;
        eventManager.register(this);
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

    private List<Event> handlePing(AgentPingEvent event)
    {
        List<Event> events = new LinkedList<Event>();
        SlaveAgent agent = (SlaveAgent) agentsById.get(event.getAgent().getId());
        if(agent == null || !agent.isEnabled() || agent.getStatus().ignorePings())
        {
            // Agent may be null if a ping was requested then the agent was
            // removed.  Similarly, it could be disabled after a ping
            // request.
            return events;
        }

        SlaveStatus pingStatus = event.getPingStatus();
        Status oldStatus = agent.getStatus();

        switch(pingStatus.getStatus())
        {
            case BUILDING:
                handlePingBuilding(agent, pingStatus, events);
                break;
            case IDLE:
                handlePingIdle(agent, pingStatus, events);
                break;
            case OFFLINE:
                handlePingOffline(agent, pingStatus, events);
                break;
            case INVALID_MASTER:
            case TOKEN_MISMATCH:
            case VERSION_MISMATCH:
                switch(oldStatus)
                {
                    case BUILDING:
                    case RECIPE_DISPATCHED:
                        events.add(new RecipeErrorEvent(this, agent.getRecipeId(), "Agent status changed to '" + pingStatus.getStatus().getPrettyString() + "' while recipe in progress"));
                        break;

                    default:
                        agent.updateStatus(pingStatus);
                        break;
                }
                break;
        }

        // Handle disable-on-idle.
        if(agent.isDisabling() && !agent.getStatus().isBusy())
        {
            events.add(new AgentEnableStateRequiredEvent(this, agent, Slave.EnableState.DISABLED));
            agent.updateStatus(Status.DISABLED);
        }

        if(agent.getStatus().isOnline())
        {
            // It is important that the agent online event comes before the
            // available event as the online event may trigger some extra
            // updates to the agent (e.g. resource discovery).
            if(!oldStatus.isOnline())
            {
                events.add(new AgentOnlineEvent(this, agent));
            }

            if(agent.getStatus() == Status.IDLE && oldStatus != Status.IDLE)
            {
                events.add(new AgentAvailableEvent(this, agent));
            }
        }
        else
        {
            if(oldStatus.isOnline())
            {
                if(agent.getStatus() != Status.IDLE && oldStatus == Status.IDLE)
                {
                    events.add(new AgentUnavailableEvent(this, agent));
                }

                events.add(new AgentOfflineEvent(this, agent));
            }

            if(pingStatus.getStatus() == PingStatus.VERSION_MISMATCH)
            {
                events.add(new AgentUpgradeRequiredEvent(this, agent));
            }
        }
        
        return events;
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
                    handleRecipeMismatch(agent, pingStatus, events);
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

    private void handleRecipeMismatch(SlaveAgent agent, SlaveStatus pingStatus, List<Event> events)
    {
        events.add(new RecipeErrorEvent(this, agent.getRecipeId(), "Agent recipe mismatch"));
        terminateRecipe(agent, pingStatus.getRecipeId());
    }

    private void terminateRecipe(SlaveAgent agent, long recipeId)
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

    private List<Event> handleRecipeStarted(RecipeDispatchedEvent event)
    {
        List<Event> events = new LinkedList<Event>();
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
                masterAgent.updateStatus(Status.BUILDING);
            }
        }

        return events;
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

    private List<Event> handleRecipeCollected(RecipeCollectedEvent event)
    {
        List<Event> events = new LinkedList<Event>();
        Agent agent = agentsByRecipeId.remove(event.getRecipeId());
        if(agent != null)
        {
            if(agent.isSlave())
            {
                SlaveAgent slaveAgent = ((SlaveAgent) agent);
                slaveAgent.updateStatus(Status.AWAITING_PING, -1);
            }
            else
            {
                if(agent.isDisabling())
                {
                    masterAgent.updateStatus(Status.DISABLED);
                    events.add(new AgentEnableStateRequiredEvent(this, agent, Slave.EnableState.DISABLED));
                    events.add(new AgentOfflineEvent(this, agent));
                }
                else
                {
                    agent.updateStatus(Status.IDLE);
                    events.add(new AgentAvailableEvent(this, agent));
                }
            }
        }

        return events;
    }

    private List<Event> handleDisableRequested(Agent agent)
    {
        List<Event> events = new LinkedList<Event>();
        switch(agent.getEnableState())
        {
            case ENABLED:
                if(agent.getStatus().isBusy())
                {
                    // Disable on idle.
                    events.add(new AgentEnableStateRequiredEvent(this, agent, Slave.EnableState.DISABLING));
                }
                else
                {
                    // Immediate disable
                    agent.updateStatus(Status.DISABLED);
                    events.add(new AgentEnableStateRequiredEvent(this, agent, Slave.EnableState.DISABLED));
                }
                break;
            
            case DISABLING:
                // Hard disable.  Raising error event will lead to us
                // disabling the agent in our handler.
                agent.getBuildService().terminateRecipe(agent.getRecipeId());
                events.add(new RecipeErrorEvent(this, agent.getRecipeId(), "Agent disabled while recipe in progress"));
                break;
        }

        return events;
    }

    private List<Event> handleEnableRequested(Agent agent)
    {
        List<Event> events = new LinkedList<Event>();
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
                    agent.updateStatus(Status.IDLE);
                }
                events.add(new AgentEnableStateRequiredEvent(this, agent, Slave.EnableState.ENABLED));
                break;
        }

        return events;
    }

    private void handleAgentAdded(SlaveAgent slaveAgent)
    {
        agentsById.put(slaveAgent.getId(), slaveAgent);
    }

    private List<Event> handleAgentRemoved(SlaveAgent slaveAgent)
    {
        List<Event> events = new LinkedList<Event>();
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

        events.add(new AgentUnavailableEvent(this, slaveAgent));
        events.add(new AgentOfflineEvent(this, slaveAgent));
        return events;
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
        List<Event> events = Collections.EMPTY_LIST;
        agentsLock.lock();
        try
        {
            if(event instanceof AgentPingEvent)
            {
                events = handlePing((AgentPingEvent) event);
            }
            else if(event instanceof RecipeDispatchedEvent)
            {
                events = handleRecipeStarted((RecipeDispatchedEvent) event);
            }
            else if(event instanceof RecipeCompletedEvent || event instanceof RecipeErrorEvent)
            {
                handleRecipeFinished((RecipeEvent) event);
            }
            else if(event instanceof RecipeCollectedEvent)
            {
                handleRecipeCollected((RecipeCollectedEvent )event);
            }
            else if(event instanceof AgentDisableRequestedEvent)
            {
                events = handleDisableRequested(((AgentEvent) event).getAgent());
            }
            else if(event instanceof AgentEnableRequestedEvent)
            {
                events = handleEnableRequested(((AgentEvent) event).getAgent());
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
                events = handleAgentRemoved(((AgentRemovedEvent) event).getSlaveAgent());
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
