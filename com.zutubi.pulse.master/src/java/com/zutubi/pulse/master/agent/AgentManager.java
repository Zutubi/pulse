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
import com.zutubi.util.adt.Pair;

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
    Agent getAgentByHandle(long handle);
    @SecureResult
    Agent getAgentById(long agentId);
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
     * Queues new synchronisation messages for the given agent.  The messages
     * will be sent on the next synchronisation cycle for the agent (and
     * retried until it succeeds or fails permanently).
     *
     * @param agent                   agent to queue the message for
     * @param messageDescriptionPairs a list of messages to queue, paired with
     *                                human-readable description of their
     *                                purpose
     */
    void enqueueSynchronisationMessages(Agent agent, List<Pair<SynchronisationMessage, String>> messageDescriptionPairs);

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
     * @param agentId id of the agent to get the messages for
     * @return all synchronisation messages for the agent
     */
    List<AgentSynchronisationMessage> getSynchronisationMessages(long agentId);

    /**
     * Returns all synchronisation messages that are in the processing state.
     * Messages for all agents are returned.
     * 
     * @return all processing synchronisation messages
     */
    List<AgentSynchronisationMessage> getProcessingSynchronisationMessages();

    /**
     * Returns the synchronisation message with the given id, if any.
     * 
     * @param messageId the database id of the message to find
     * @return the message with the given id, or null if there is no such
     *         message
     */
    AgentSynchronisationMessage getSynchronisationMessage(long messageId);
    
    /**
     * Attempts to complete the synchronisation cycle for the given agent and
     * send out the relevant event.  This may fail if there are new messages
     * that should be processed now.
     *
     * @param agentId    id of the agent to complete the cycle for if possible
     * @param successful indicates if sending of the last batch of messages
     *                   succeeded (if not, the event is always sent indicating
     *                   the cycle should be retried later)
     * @return true if the cycle is complete, false if it should re-run because
     *         new messages are available
     */
    boolean completeSynchronisation(long agentId, boolean successful);

    /**
     * Updates the agent state, ensuring that it persists and that the agent
     * itself is also updated.  Agent state should always be updated using this
     * manager, so cached agents reflect any changes made.
     *
     * @param agent    the agent to update the state of
     * @param updateFn a callback that can make desired changes to the state
     */
    public void updateAgentState(Agent agent, UnaryProcedure<AgentState> updateFn);
}
