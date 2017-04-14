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

package com.zutubi.pulse.master.security;

import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.tove.security.AuthorityProvider;
import com.zutubi.tove.security.DefaultAccessManager;

import java.util.Set;

/**
 * Maps from agent ACL configurations to authorities allowed to perform
 * actions on agents.
 */
public class AgentAuthorityProvider implements AuthorityProvider<Agent>
{
    private AgentConfigurationAuthorityProvider agentConfigurationAuthorityProvider;

    public Set<String> getAllowedAuthorities(String action, Agent resource)
    {
        return agentConfigurationAuthorityProvider.getAllowedAuthorities(action, resource.getConfig());
    }

    public void setAccessManager(DefaultAccessManager accessManager)
    {
        accessManager.registerAuthorityProvider(Agent.class, this);
    }

    public void setAgentConfigurationAuthorityProvider(AgentConfigurationAuthorityProvider agentConfigurationAuthorityProvider)
    {
        this.agentConfigurationAuthorityProvider = agentConfigurationAuthorityProvider;
    }
}
