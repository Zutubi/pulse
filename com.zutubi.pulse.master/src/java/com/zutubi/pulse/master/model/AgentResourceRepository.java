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

import com.zutubi.pulse.core.ResourceRepositorySupport;
import com.zutubi.pulse.core.resources.api.ResourceConfiguration;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;

import java.util.Map;

/**
 * A resource repository backed by the configuration subsystem.
 */
public class AgentResourceRepository extends ResourceRepositorySupport
{
    private AgentConfiguration agentConfig;

    public AgentResourceRepository(AgentConfiguration agentConfig)
    {
        this.agentConfig = agentConfig;
    }

    public ResourceConfiguration getResource(String name)
    {
        return agentConfig.getResources().get(name);
    }

    public AgentConfiguration getAgentConfig()
    {
        return agentConfig;
    }

    public Map<String, ResourceConfiguration> getAll()
    {
        return agentConfig.getResources();
    }
}
