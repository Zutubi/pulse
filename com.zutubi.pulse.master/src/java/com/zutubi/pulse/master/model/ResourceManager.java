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

package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.ResourceRepository;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public interface ResourceManager
{
    ResourceRepository getAgentRepository(long handle);

    ResourceRepository getAgentRepository(AgentConfiguration agent);

    ResourceRepository getAgentRepository(Agent agent);

    /**
     * Returns the set of agents that are capable of running a recipe with the
     * given resource requirements.  All non-optional requirements must be
     * present in the agent's resource repository for it to be capable.
     *
     * @param requirements the requirements to test for
     * @return the set of all agents that can satisfy the requirements
     */
    Set<AgentConfiguration> getCapableAgents(Collection<? extends ResourceRequirement> requirements);

    List<AgentConfiguration> addDiscoveredResources(String location, List<ResourceConfiguration> resources);

    /**
     * Returns all resources from all agents visible to the calling user, mapped by agent name.
     *
     * @return a mapping from agent name to resources on that agent
     */
    Map<String, List<ResourceConfiguration>> findAllVisible();
}
