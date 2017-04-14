/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
     * Returns the host for the given location.
     *
     * @param location the location to get the host for
     * @return the host for the location, or null if there is no such host
     */
    public Host getHostForLocation(String location);

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
