package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.agent.statistics.AgentStatistics;
import com.zutubi.pulse.master.model.AgentState;
import com.zutubi.pulse.master.model.AgentSynchronisationMessage;
import com.zutubi.pulse.master.security.SecureParameter;
import com.zutubi.pulse.master.security.SecureResult;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfigurationActions;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.tove.security.AccessManager;
import com.zutubi.util.UnaryProcedure;

import java.util.List;

/**
 */
public interface AgentManager extends AgentPersistentStatusManager
{
    String GLOBAL_AGENT_NAME = "global agent template";
    String MASTER_AGENT_NAME = "master";

    @SecureResult
    List<Agent> getAllAgents();
    @SecureResult
    List<Agent> getOnlineAgents();
    @SecureResult
    Agent getAgent(long handle);
    @SecureResult
    Agent getAgent(AgentConfiguration agent);
    @SecureResult
    Agent getAgent(String name);

    @SecureParameter(parameterType = AgentConfiguration.class, action = AccessManager.ACTION_DELETE)
    void deleteState(AgentState state);

    @SecureParameter(parameterType = AgentConfiguration.class, action = AgentConfigurationActions.ACTION_PING)
    void pingAgent(AgentConfiguration agentConfig);

    int getAgentCount();

    /**
     * Runs a callback with available agents, locking to ensure such
     * availability does not change while the callback is running.  Note that
     * this means the callback must run very quickly to avoid blocking other
     * state changes.
     *
     * @param fn callback to run, passed all available agents
     */
    void withAvailableAgents(UnaryProcedure<List<Agent>> fn);

    /**
     * Returns statistics gathered for the given agent.
     *
     * @param agent the agent to get statistics for
     * @return statistics for the given agent
     */
    @SecureParameter(parameterType = Agent.class, action = AccessManager.ACTION_VIEW)
    AgentStatistics getAgentStatistics(Agent agent);

    /**
     * Instructs the manager to update the agent statistics for this point in
     * time.  Should be called regularly to keep statistics current.
     */
    void updateStatistics();

    /**
     * Queues a new synchronisation message for the given agent.  The message
     * will be sent on the next synchronisation cycle for the agent (and
     * retried until it succeeds or fails permanently).
     *
     * @param agent       agent to queue the message for
     * @param message     the message to queue
     * @param description human-readable description of the purpose of this
     *                    message
     */
    void enqueueSynchronisationMessage(Agent agent, SynchronisationMessage message, String description);

    /**
     * Dequeues (deletes) the given agent synchronisation messages.
     *
     * @param messages the messages to delete
     */
    void dequeueSynchronisationMessages(List<AgentSynchronisationMessage> messages);

    /**
     * Saves changes to the given agent synchronisation messages.
     *
     * @param messages the messages to save
     */
    void saveSynchronisationMessages(List<AgentSynchronisationMessage> messages);

    /**
     * Returns all synchronisation messages for the given agent, in the order
     * that they were enqueued.  Note that some messages may already be
     * complete.
     *
     * @param agent agent to get the messages for
     * @return all synchronisation messages for the agent
     */
    List<AgentSynchronisationMessage> getSynchronisationMessages(Agent agent);

    /**
     * Attempts to complete the synchronisation cycle for the given agent and
     * send out the relevant event.  This may fail if there are new messages
     * that should be processed now.
     *
     * @param agent      agent to try to complete the cycle for
     * @param successful indicates if sending of the last batch of messages
     *                   succeeded (if not, the event is always sent indicating
     *                   the cycle should be retried later)
     * @return true if the cycle is complete, false if it should re-run
     */
    boolean completeSynchronisation(Agent agent, boolean successful);
}
