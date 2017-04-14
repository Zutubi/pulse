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

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.i18n.Messages;

/**
 * Requirements that only allow a stage to be dispatch to a specified agent.
 */
public class SpecificAgentRequirements implements AgentRequirements
{
    private static final Messages I18N  = Messages.getInstance(SpecificAgentRequirements.class);
    
    private AgentConfiguration agent;

    public SpecificAgentRequirements(AgentConfiguration agent)
    {
        this.agent = agent;
    }

    public String getSummary()
    {
        return I18N.format("summary", agent.getName());
    }

    public boolean isFulfilledBy(RecipeAssignmentRequest request, AgentService service)
    {
        return service.getAgentConfig().equals(agent);
    }

    public String getUnfulfilledReason(RecipeAssignmentRequest request)
    {
        return I18N.format("unfulfilled.reason", agent.getName());
    }
}
