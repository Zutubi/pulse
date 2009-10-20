package com.zutubi.pulse.master.agent;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.core.events.RecipeCompletedEvent;
import com.zutubi.pulse.core.events.RecipeErrorEvent;
import com.zutubi.pulse.core.events.RecipeEvent;
import com.zutubi.pulse.master.events.*;
import com.zutubi.pulse.master.events.build.*;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.tove.config.admin.AgentPingConfiguration;
import com.zutubi.pulse.servercore.agent.PingStatus;
import com.zutubi.pulse.servercore.agent.Status;
import com.zutubi.pulse.servercore.services.SlaveStatus;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
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
    private static final Logger LOG = Logger.getLogger(AgentStatusManager.class);

    private Map<Long, Agent> agentsById = new HashMap<Long, Agent>();
    private Map<Long, Agent> agentsByRecipeId = new HashMap<Long, Agent>();
    private Lock agentsLock = new ReentrantLock();
    private Executor eventPump;
    private AgentPersistentStatusManager agentPersistentStatusManager;
    private EventManager eventManager;
    private ConfigurationProvider configurationProvider;

    public AgentStatusManager(AgentPersistentStatusManager agentPersistentStatusManager, Executor eventPump, EventManager eventManager, ConfigurationProvider configurationProvider)
    {
        this.agentPersistentStatusManager = agentPersistentStatusManager;
        this.eventPump = eventPump;
        this.eventManager = eventManager;
        eventManager.register(this);
        this.configurationProvider = configurationProvider;
    }

    public long getAgentOfflineTimeout()
    {
        return configurationProvider.get(AgentPingConfiguration.class).getOfflineTimeout();
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

    private void handlePing(Agent agent, SlaveStatus pingStatus)
    {
        agent = agentsById.get(agent.getId());
        if(agent == null || !agent.isEnabled() || agent.getStatus().ignorePings())
        {
            // Agent may be null if a ping was requested then the agent was
            // removed.  Similarly, it could be disabled after a ping
            // request.
            return;
        }

        checkForAgentBounce(agent, pingStatus);

        Status oldStatus = agent.getStatus();

        switch(pingStatus.getStatus())
        {
            case BUILDING:
                handlePingBuilding(agent, pingStatus);
                break;
            case IDLE:
                handlePingIdle(agent, pingStatus);
                break;
            case OFFLINE:
                handlePingOffline(agent, pingStatus);
                break;
            case INVALID_MASTER:
            case TOKEN_MISMATCH:
            case VERSION_MISMATCH:
                switch(oldStatus)
                {
                    case BUILDING:
                    case RECIPE_ASSIGNED:
                        publishEvent(new RecipeErrorEvent(this, agent.getRecipeId(), "Agent status changed to '" + pingStatus.getStatus().getPrettyString() + "' while recipe in progress"));

                        // So severe that we will not do the usual post
                        // recipe jazz.  This agent is gone proper.
                        agentsByRecipeId.remove(agent.getRecipeId());
                        agent.updateStatus(pingStatus);
                        break;

                    default:
                        agent.updateStatus(pingStatus);
                        break;
                }
                break;
        }

        // Catch-all for disable-on-idle.
        if(agent.isDisabling() && !agent.getStatus().isBusy())
        {
            agentPersistentStatusManager.setEnableState(agent, AgentState.EnableState.DISABLED);
            agent.updateStatus(Status.DISABLED);
        }

        if(agent.getStatus().isOnline())
        {
            if(!oldStatus.isOnline())
            {
                publishEvent(new AgentOnlineEvent(this, agent));
            }

            if(agent.getStatus() == Status.IDLE && oldStatus != Status.IDLE)
            {
                publishEvent(new AgentAvailableEvent(this, agent));
            }
        }
        else
        {
            if(oldStatus.isOnline())
            {
                if(agent.getStatus() != Status.IDLE && oldStatus == Status.IDLE)
                {
                    publishEvent(new AgentUnavailableEvent(this, agent));
                }

                publishEvent(new AgentOfflineEvent(this, agent));
            }

            if(pingStatus.getStatus() == PingStatus.VERSION_MISMATCH)
            {
                publishEvent(new AgentUpgradeRequiredEvent(this, agent));
            }
        }
    }

    private void checkForAgentBounce(Agent agent, SlaveStatus pingStatus)
    {
        if(agent.getStatus().isOnline() && pingStatus.isFirst())
        {
            // The agent must have bounced between pings.  Simulate the
            // master seeing this by sending an offline ping.
            handlePing(agent, new SlaveStatus(PingStatus.OFFLINE));
        }
    }

    private void handlePingBuilding(Agent agent, SlaveStatus pingStatus)
    {
        long pingRecipe = pingStatus.getRecipeId();
        switch (agent.getStatus())
        {
            case RECIPE_ASSIGNED:
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
                    // the expected recipe (when we get the collecting
                    // event, stimulated by this error event).  Once that is
                    // done the awaiting ping state will move us the
                    // appropriate next status.
                    publishEvent(new RecipeErrorEvent(this, agent.getRecipeId(), "Agent recipe mismatch"));
                    publishEvent(new RecipeTerminateRequestEvent(this, agent.getService(), pingStatus.getRecipeId()));
                }
                break;

            case AWAITING_PING:
                // If the same recipe id this could just be a race, ignore
                // such pings up until the timeout.
                if (pingRecipe != agent.getRecipeId() || agent.getSecondsSincePing() > getAgentOfflineTimeout())
                {
                    publishEvent(new RecipeTerminateRequestEvent(this, agent.getService(), pingRecipe));
                    agent.updateStatus(Status.BUILDING_INVALID, pingRecipe);
                }
                break;

            case BUILDING_INVALID:
                // Terminate it again.
                publishEvent(new RecipeTerminateRequestEvent(this, agent.getService(), pingRecipe));
                break;

            default:
                publishEvent(new RecipeTerminateRequestEvent(this, agent.getService(), pingRecipe));
                agent.updateStatus(Status.BUILDING_INVALID, pingRecipe);
                break;
        }
    }

    private void handlePingIdle(Agent agent, SlaveStatus pingStatus)
    {
        switch (agent.getStatus())
        {
            case BUILDING:
                // Can happen legitimately, but if we pass the
                // timeout then presume something is wrong.
                checkForStatusTimeout(agent, "Agent idle before recipe expected to complete");
                break;

            case BUILDING_INVALID:
                agent.updateStatus(pingStatus);
                break;

            case RECIPE_ASSIGNED:
                checkForStatusTimeout(agent, "Agent idle after recipe expected to have commenced");
                break;

            default:
                agent.updateStatus(pingStatus);
                break;
        }
    }

    private void handlePingOffline(Agent agent, SlaveStatus pingStatus)
    {
        switch (agent.getStatus())
        {
            case BUILDING:
            case RECIPE_ASSIGNED:
                // Don't immediately give up - wait for the timeout.
                if (checkForStatusTimeout(agent, "Connection to agent lost during recipe execution"))
                {
                    agent.updateStatus(pingStatus);
                }
                break;

            default:
                agent.updateStatus(pingStatus);
                break;
        }
    }

    private boolean checkForStatusTimeout(Agent agent, String message)
    {
        long timeSincePing = agent.getSecondsSincePing();
        long timeout = getAgentOfflineTimeout();
        if (timeSincePing > timeout)
        {
            publishEvent(new RecipeErrorEvent(this, agent.getRecipeId(), message + " (agent: " + agent.getName() + ", recipe: " + agent.getRecipeId() + ", since ping: " + timeSincePing + ", timeout: " + timeout + ")"));
            return true;
        }

        return false;
    }

    private void handleRecipeAssigned(RecipeAssignedEvent event)
    {
        Agent agent = agentsById.get(event.getAgent().getId());
        if(agent != null)
        {
            agentsByRecipeId.put(event.getRecipeId(), agent);
            publishEvent(new AgentUnavailableEvent(this, agent));
            agent.updateStatus(Status.RECIPE_ASSIGNED, event.getRecipeId());
        }
    }

    private void handleRecipeCollecting(RecipeEvent event)
    {
        Agent agent = agentsByRecipeId.get(event.getRecipeId());
        if(agent != null)
        {
            agent.updateStatus(Status.POST_RECIPE, event.getRecipeId());
        }
    }

    private void handleRecipeCompleted(long recipeId)
    {
        Agent agent = agentsByRecipeId.remove(recipeId);
        if(agent != null)
        {
            if (agent.isDisabling())
            {
                agent.updateStatus(Status.DISABLED);
                agentPersistentStatusManager.setEnableState(agent, AgentState.EnableState.DISABLED);
                publishEvent(new AgentOfflineEvent(this, agent));
            }
            else
            {
                agent.updateStatus(Status.AWAITING_PING, recipeId);

                // Request a ping immediately so no time is wasted
                publishEvent(new AgentPingRequestedEvent(this, agent));
            }
        }
    }

    private void handleDisableRequested(Agent agent)
    {
        Status status = agent.getStatus();
        if (status == Status.AWAITING_PING)
        {
            // Small optimisation: no need to wait anymore.
            disableAgent(agent);
        }
        else
        {
            switch (agent.getEnableState())
            {
                case ENABLED:
                    if (status.isBusy())
                    {
                        // Disable on idle.
                        agentPersistentStatusManager.setEnableState(agent, AgentState.EnableState.DISABLING);
                    }
                    else
                    {
                        // Immediate disable
                        if (agent.isOnline())
                        {
                            publishEvent(new AgentUnavailableEvent(this, agent));
                        }

                        disableAgent(agent);
                    }
                    break;

                case DISABLING:
                    // Hard disable.  Raising error event will lead to a
                    // collecting event and we will disable the agent in our
                    // handler.
                    switch (status)
                    {
                        case BUILDING:
                        case RECIPE_ASSIGNED:
                            publishEvent(new RecipeTerminateRequestEvent(this, agent.getService(), agent.getRecipeId()));
                            publishEvent(new RecipeErrorEvent(this, agent.getRecipeId(), "Agent disabled while recipe in progress"));
                            break;
                   }
                    break;
            }
        }
    }

    private void disableAgent(Agent agent)
    {
        if (agent.isOnline())
        {
            publishEvent(new AgentOfflineEvent(this, agent));
        }

        agent.updateStatus(Status.DISABLED);
        agentPersistentStatusManager.setEnableState(agent, AgentState.EnableState.DISABLED);
    }

    private void handleEnableRequested(Agent agent)
    {
        switch(agent.getEnableState())
        {
            case DISABLING:
                // Cancel disable-on-idle.
                agentPersistentStatusManager.setEnableState(agent, AgentState.EnableState.ENABLED);
                break;

            case DISABLED:
            case FAILED_UPGRADE:
                agent.updateStatus(Status.INITIAL);

                // Request a ping now to save time
                publishEvent(new AgentPingRequestedEvent(this, agent));
                agentPersistentStatusManager.setEnableState(agent, AgentState.EnableState.ENABLED);
                break;
        }
    }

    private void handleAgentAdded(Agent agent)
    {
        agentsById.put(agent.getId(), agent);
    }

    private void handleAgentRemoved(Agent agent)
    {
        agentsById.remove(agent.getId());

        // If the agent is running a recipe remove it, and try to terminate
        // the recipe controller with an error if we are not too late.
        long recipeId = findRecipeId(agent);
        if(recipeId > 0)
        {
            Agent runningAgent = agentsByRecipeId.remove(recipeId);
            switch(runningAgent.getStatus())
            {
                case RECIPE_ASSIGNED:
                case BUILDING:
                    publishEvent(new RecipeErrorEvent(this, recipeId, "Agent deleted while recipe in progress"));
                    break;
            }
        }

        Status status = agent.getStatus();
        if(status.isOnline())
        {
            if (!status.isBusy())
            {
                publishEvent(new AgentUnavailableEvent(this, agent));
            }

            publishEvent(new AgentOfflineEvent(this, agent));
        }
    }

    private void handleAgentChanged(Agent agent)
    {
        Agent existingAgent = agentsById.get(agent.getId());
        if(existingAgent != null)
        {
            agent.copyStatus(existingAgent);
        }

        agentsById.put(agent.getId(), agent);

        long recipeId = findRecipeId(agent);
        if(recipeId > 0)
        {
            agentsByRecipeId.put(recipeId, agent);
        }
    }

    private long findRecipeId(Agent agent)
    {
        long recipeId = 0;
        for(Map.Entry<Long, Agent> entry: agentsByRecipeId.entrySet())
        {
            if(entry.getValue().getId() == agent.getId())
            {
                recipeId = entry.getKey();
            }
        }
        return recipeId;
    }

    public void publishEvent(final Event event)
    {
        eventPump.execute(new Runnable()
        {
            public void run()
            {
                eventManager.publish(event);
            }
        });
    }

    public void handleEvent(Event event)
    {
        long startTime = System.currentTimeMillis();
        agentsLock.lock();
        try
        {
            if(event instanceof AgentPingEvent)
            {
                AgentPingEvent ape = (AgentPingEvent) event;
                handlePing(ape.getAgent(), ape.getPingStatus());
            }
            else if(event instanceof RecipeAssignedEvent)
            {
                handleRecipeAssigned((RecipeAssignedEvent) event);
            }
            else if(event instanceof RecipeCollectingEvent)
            {
                handleRecipeCollecting((RecipeEvent) event);
            }
            else if(event instanceof RecipeCollectedEvent)
            {
                handleRecipeCompleted(((RecipeCollectedEvent) event).getRecipeId());
            }
            else if(event instanceof RecipeAbortedEvent)
            {
                handleRecipeCompleted(((RecipeAbortedEvent) event).getRecipeId());
            }
            else if(event instanceof AgentDisableRequestedEvent)
            {
                handleDisableRequested(((AgentEvent) event).getAgent());
            }
            else if(event instanceof AgentEnableRequestedEvent)
            {
                handleEnableRequested(((AgentEvent) event).getAgent());
            }
            else if(event instanceof AgentAddedEvent)
            {
                handleAgentAdded(((AgentAddedEvent) event).getAgent());
            }
            else if(event instanceof AgentChangedEvent)
            {
                handleAgentChanged(((AgentChangedEvent) event).getAgent());
            }
            else if(event instanceof AgentRemovedEvent)
            {
                handleAgentRemoved(((AgentRemovedEvent) event).getAgent());
            }
        }
        finally
        {
            agentsLock.unlock();
        }

        // This handler should be fast, as it is synchronous and cannot
        // steal large amounts of time from the publisher.
        long elapsedMillis = System.currentTimeMillis() - startTime;
        if(elapsedMillis > 5000)
        {
            LOG.warning("Processing event '" + event.toString() + "' took more than " + (elapsedMillis / 1000) + " seconds");
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
                RecipeAbortedEvent.class,
                RecipeCollectedEvent.class,
                RecipeCollectingEvent.class,
                RecipeCompletedEvent.class,
                RecipeAssignedEvent.class,
        };
    }
}
