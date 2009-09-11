package com.zutubi.pulse.master.agent;

import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;

import java.util.Collection;

/**
 * Interface for management of hosts.  Supports the creation and maintenance of
 * hosts for agents, including the detection of shared hosts (i.e. those used
 * by multiple agents).
 */
public interface HostManager
{
    /**
     * Initialises this host manager.  To be called during agent manager
     * startup.
     *
     * @param agentManager used for agent management
     */
    void init(AgentManager agentManager);

    /**
     * Registers the addition of a new agent, returning the corresponding host
     * (created if necessary).
     *
     * @param agentConfig the agent that has been added
     * @return the host used by the agent
     */
    Host agentAdded(AgentConfiguration agentConfig);

    /**
     * Registers a change to an agent, potentially changing its host.
     *
     * @param agentConfig the new agent configuration
     * @return the host used by the agent
     */
    Host agentChanged(AgentConfiguration agentConfig);

    /**
     * Registers the removal of an agent, potentially cleaning up its host.
     *
     * @param agentConfig the agent that has been deleted
     */
    void agentDeleted(AgentConfiguration agentConfig);

    /**
     * Returns the host that is to be used for the given agent.  The agent must
     * have been previously registered using {@link #agentAdded(com.zutubi.pulse.master.tove.config.agent.AgentConfiguration)}.
     *
     * @param agentConfig the agent to retrieve the host for
     * @return the host used by the given agent
     */
    Host getHostForAgent(AgentConfiguration agentConfig);

    /**
     * Returns all agents that use the given host.
     *
     * @param host the host to retrieve the agents for
     * @return all agents that use the given host
     */
    Collection<Agent> getAgentsForHost(Host host);

    /**
     * Retrieves a service that can be used to communicate with a host.
     *
     * @param host the host to retrieve the service for
     * @return a service that can be used to communicate with the host
     */
    HostService getServiceForHost(Host host);

    /**
     * Requests a ping of all hosts.
     */
    void pingHosts();

    /**
     * Requests a ping of the given host.
     *
     * @param host the host to ping
     */
    void pingHost(Host host);
}
