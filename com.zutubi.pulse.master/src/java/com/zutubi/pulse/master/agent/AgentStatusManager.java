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
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.NullaryFunction;
import com.zutubi.util.Predicate;
import com.zutubi.util.logging.Logger;

import java.util.*;
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
    private Set<Long> preemptivelyCompletedRecipes = new HashSet<Long>();
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

    public List<Agent> getAgentsByStatusPredicate(Predicate<AgentStatus> predicate)
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

    /**
     * Allows a callback to be run under the agents lock.  During this lock no
     * agent states will change.  Note that this lock can only be held for a
     * very short time, so the given callback musts be fast.  This method is
     * package local so as not to spread the ability to take this lock too
     * widely.  Instead, wrap required logic up into more user-friendly APIs
     * in, e.g. the {@link AgentManager}.
     *
     * @param fn callback to run under the lock
     * @return the return from the callback
     */
    <T> T withAgentsLock(NullaryFunction<T> fn)
    {
        agentsLock.lock();
        try
        {
            return fn.process();
        }
        finally
        {
            agentsLock.unlock();
        }
    }

    private void handlePing(AgentPingEvent agentPingEvent)
    {
        Agent agent = agentsById.get(agentPingEvent.getAgent().getId());
        if (agent == null || !agent.isEnabled() || agent.getStatus().isIgnorePings())
        {
            // Agent may be null if a ping was requested then the agent was
            // removed.  Similarly, it could be disabled after a ping
            // request.
            return;
        }

        checkForAgentBounce(agent, agentPingEvent);

        AgentStatus oldStatus = agent.getStatus();

        switch(agentPingEvent.getPingStatus())
        {
            case BUILDING:
                handlePingBuilding(agent, agentPingEvent);
                break;
            case IDLE:
                handlePingIdle(agent, agentPingEvent);
                break;
            case OFFLINE:
                handlePingOffline(agent, agentPingEvent);
                break;
            case INVALID_MASTER:
            case TOKEN_MISMATCH:
            case VERSION_MISMATCH:
                switch(oldStatus)
                {
                    case BUILDING:
                    case RECIPE_ASSIGNED:
                        publishEvent(new RecipeErrorEvent(this, agent.getRecipeId(), "Agent status changed to '" + agentPingEvent.getPingStatus().getPrettyString() + "' while recipe in progress"));

                        // So severe that we will not do the usual post
                        // recipe jazz.  This agent is gone proper.
                        agentsByRecipeId.remove(agent.getRecipeId());
                        agent.updateStatus(agentPingEvent);
                        break;

                    default:
                        agent.updateStatus(agentPingEvent);
                        break;
                }
                break;
        }

        // Catch-all for disable-on-idle.
        if (agent.isDisabling() && agent.getStatus().isAvailable())
        {
            agentPersistentStatusManager.setEnableState(agent, AgentState.EnableState.DISABLED);
            agent.updateStatus(AgentStatus.DISABLED);
        }

        AgentStatus newStatus = agent.getStatus();
        if (newStatus.isOnline())
        {
            if (!oldStatus.isOnline())
            {
                publishEvent(new AgentOnlineEvent(this, agent));
            }

            if (newStatus.isAvailable() && !oldStatus.isAvailable())
            {
                publishEvent(new AgentAvailableEvent(this, agent));
            }
        }
        else
        {
            if (oldStatus.isOnline())
            {
                if (!newStatus.isAvailable() && oldStatus.isAvailable())
                {
                    publishEvent(new AgentUnavailableEvent(this, agent));
                }

                publishEvent(new AgentOfflineEvent(this, agent));
            }
        }

        if (oldStatus != newStatus)
        {
            publishEvent(new AgentStatusChangeEvent(this, agent, oldStatus, newStatus));
        }
    }

    private void checkForAgentBounce(Agent agent, AgentPingEvent agentPingEvent)
    {
        if (agent.getStatus().isOnline() && agentPingEvent.isFirst())
        {
            // The agent must have bounced between pings.  Simulate the
            // master seeing this by sending an offline ping.
            handlePing(new AgentPingEvent(this, agent, PingStatus.OFFLINE));
        }
    }

    private void handlePingBuilding(Agent agent, AgentPingEvent agentPingEvent)
    {
        long pingRecipe = agentPingEvent.getRecipeId();
        switch (agent.getStatus())
        {
            case RECIPE_ASSIGNED:
            case BUILDING:
                if (pingRecipe == agent.getRecipeId())
                {
                    // Expected case.
                    agent.updateStatus(agentPingEvent);
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
                    publishEvent(new RecipeTerminateRequestEvent(this, agent.getService(), agentPingEvent.getRecipeId()));
                }
                break;

            case AWAITING_PING:
                // If the same recipe id this could just be a race, ignore
                // such pings up until the timeout.
                if (pingRecipe != agent.getRecipeId() || agent.getSecondsSincePing() > getAgentOfflineTimeout())
                {
                    publishEvent(new RecipeTerminateRequestEvent(this, agent.getService(), pingRecipe));
                    agent.updateStatus(AgentStatus.BUILDING_INVALID, pingRecipe);
                }
                break;

            case BUILDING_INVALID:
                // Terminate it again.
                publishEvent(new RecipeTerminateRequestEvent(this, agent.getService(), pingRecipe));
                break;

            default:
                publishEvent(new RecipeTerminateRequestEvent(this, agent.getService(), pingRecipe));
                agent.updateStatus(AgentStatus.BUILDING_INVALID, pingRecipe);
                break;
        }
    }

    private void handlePingIdle(Agent agent, AgentPingEvent agentPingEvent)
    {
        switch (agent.getStatus())
        {
            case BUILDING:
                // Can happen legitimately, but if we pass the
                // timeout then presume something is wrong.
                checkForStatusTimeout(agent, "Agent idle before recipe expected to complete");
                break;

            case BUILDING_INVALID:
                agent.updateStatus(agentPingEvent);
                break;

            case RECIPE_ASSIGNED:
                checkForStatusTimeout(agent, "Agent idle after recipe expected to have commenced");
                break;

            case IDLE:
            case SYNCHRONISED:
                agent.updateStatus(agentPingEvent);
                break;

            default:
                agent.updateStatus(AgentStatus.SYNCHRONISING);
                break;
        }
    }

    private void handlePingOffline(Agent agent, AgentPingEvent agentPingEvent)
    {
        switch (agent.getStatus())
        {
            case BUILDING:
            case RECIPE_ASSIGNED:
                // Don't immediately give up - wait for the timeout.
                if (checkForStatusTimeout(agent, "Connection to agent lost during recipe execution"))
                {
                    agent.updateStatus(agentPingEvent);
                }
                break;

            default:
                agent.updateStatus(agentPingEvent);
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

    private void handleSynchronisationComplete(AgentSynchronisationCompleteEvent event)
    {
        Agent agent = agentsById.get(event.getAgent().getId());
        if (agent != null)
        {
            AgentStatus oldStatus = agent.getStatus();
            if (oldStatus == AgentStatus.SYNCHRONISING)
            {
                if (agent.isDisabling())
                {
                    agent.updateStatus(AgentStatus.DISABLED);
                    agentPersistentStatusManager.setEnableState(agent, AgentState.EnableState.DISABLED);
                    publishEvent(new AgentOfflineEvent(this, agent));
                }
                else
                {
                    if (event.isSuccessful())
                    {
                        agent.updateStatus(AgentStatus.SYNCHRONISED);
                    }
                    else
                    {
                        agent.updateStatus(AgentStatus.OFFLINE);
                        publishEvent(new AgentOfflineEvent(this, agent));
                    }
    
                    publishEvent(new AgentPingRequestedEvent(this, agent));
                }
    
                publishEvent(new AgentStatusChangeEvent(this, agent, oldStatus, agent.getStatus()));
            }
            else
            {
                LOG.warning("Synchronisation complete event for agent '" + agent.getName() + "' which is in state '" + agent.getStatus() + "'");
            }
        }
    }

    private void handleRecipeAssigned(RecipeAssignedEvent event)
    {
        Agent agent = agentsById.get(event.getAgent().getId());
        if(agent != null)
        {
            long recipeId = event.getRecipeId();
            if (!preemptivelyCompletedRecipes.remove(recipeId))
            {
                agentsByRecipeId.put(recipeId, agent);
                publishEvent(new AgentUnavailableEvent(this, agent));
                updateAgentRecipeStatus(agent, AgentStatus.RECIPE_ASSIGNED, recipeId);
            }
        }
    }

    private void handleRecipeCollecting(RecipeEvent event)
    {
        Agent agent = agentsByRecipeId.get(event.getRecipeId());
        if(agent != null)
        {
            updateAgentRecipeStatus(agent, AgentStatus.POST_RECIPE, event.getRecipeId());
        }
    }

    private void handleRecipeCompleted(long recipeId)
    {
        Agent agent = agentsByRecipeId.remove(recipeId);
        if (agent != null)
        {
            if (agent.isDisabling())
            {
                updateAgentRecipeStatus(agent, AgentStatus.DISABLED, -1);
                agentPersistentStatusManager.setEnableState(agent, AgentState.EnableState.DISABLED);
                publishEvent(new AgentOfflineEvent(this, agent));
            }
            else
            {
                updateAgentRecipeStatus(agent, AgentStatus.AWAITING_PING, recipeId);

                // Request a ping immediately so no time is wasted
                publishEvent(new AgentPingRequestedEvent(this, agent));
            }
        }
        else
        {
            // This can happen when the build controller bails out of a build
            // when there are pending recipe dispatch (which are managed
            // asynchronously by the recipe queue).
            preemptivelyCompletedRecipes.add(recipeId);
        }
    }

    private void updateAgentRecipeStatus(Agent agent, AgentStatus newStatus, long recipeId)
    {
        AgentStatus oldStatus = agent.getStatus();
        agent.updateStatus(newStatus, recipeId);
        publishEvent(new AgentStatusChangeEvent(this, agent, oldStatus, agent.getStatus()));
    }

    private void handleDisableRequested(Agent agent)
    {
        AgentStatus status = agent.getStatus();
        if (status == AgentStatus.AWAITING_PING)
        {
            // Small optimisation: no need to wait anymore.
            disableAgent(agent);
        }
        else
        {
            switch (agent.getEnableState())
            {
                case ENABLED:
                    if (status.isOnline())
                    {
                        if (status.isAvailable())
                        {
                            // Immediate disable
                            publishEvent(new AgentUnavailableEvent(this, agent));
                            disableAgent(agent);
                        }
                        else
                        {
                            // Disable on idle.
                            agentPersistentStatusManager.setEnableState(agent, AgentState.EnableState.DISABLING);
                        }
                    }
                    else
                    {
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
        AgentStatus oldStatus = agent.getStatus();
        if (agent.isOnline())
        {
            publishEvent(new AgentOfflineEvent(this, agent));
        }

        agentPersistentStatusManager.setEnableState(agent, AgentState.EnableState.DISABLED);
        agent.updateStatus(AgentStatus.DISABLED);
        publishEvent(new AgentStatusChangeEvent(this, agent, oldStatus, agent.getStatus()));
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
                AgentStatus oldStatus = agent.getStatus();
                agentPersistentStatusManager.setEnableState(agent, AgentState.EnableState.ENABLED);
                agent.updateStatus(AgentStatus.INITIAL);
                publishEvent(new AgentStatusChangeEvent(this, agent, oldStatus, agent.getStatus()));

                // Request a ping now to save time
                publishEvent(new AgentPingRequestedEvent(this, agent));
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

        AgentStatus status = agent.getStatus();
        if (status.isOnline())
        {
            if (status.isAvailable())
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

    private void handleAgentSynchronisationMessagesEnqueued(Agent agent)
    {
        Agent existingAgent = agentsById.get(agent.getId());
        if (existingAgent != null)
        {
            AgentStatus currentStatus = existingAgent.getStatus();
            if (currentStatus == AgentStatus.IDLE || currentStatus == AgentStatus.SYNCHRONISED)
            {
                existingAgent.updateStatus(AgentStatus.SYNCHRONISING);
                publishEvent(new AgentStatusChangeEvent(this, existingAgent, currentStatus, AgentStatus.SYNCHRONISING));
            }
        }
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
                handlePing((AgentPingEvent) event);
            }
            else if (event instanceof AgentSynchronisationCompleteEvent)
            {
                handleSynchronisationComplete((AgentSynchronisationCompleteEvent)event);
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
            else if (event instanceof AgentSynchronisationMessagesEnqueuedEvent)
            {
                handleAgentSynchronisationMessagesEnqueued(((AgentSynchronisationMessagesEnqueuedEvent) event).getAgent());
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
                AgentSynchronisationMessagesEnqueuedEvent.class,
                AgentSynchronisationCompleteEvent.class,
                RecipeAbortedEvent.class,
                RecipeCollectedEvent.class,
                RecipeCollectingEvent.class,
                RecipeCompletedEvent.class,
                RecipeAssignedEvent.class,
        };
    }
}
