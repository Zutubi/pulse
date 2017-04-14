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
import com.zutubi.i18n.Messages;

/**
 * Ensures that personal build assignment requests are only fulfilled by agents
 * which allow personal builds.
 */
public class PersonalBuildAgentRequirements implements AgentRequirements
{
    private static final Messages I18N = Messages.getInstance(PersonalBuildAgentRequirements.class);
    
    private AgentRequirements agentRequirements;

    public PersonalBuildAgentRequirements(AgentRequirements agentRequirements)
    {
        this.agentRequirements = agentRequirements;
    }

    private boolean verifyPersonalBuilds(RecipeAssignmentRequest request, AgentService service)
    {
        return !request.isPersonal() || service.getAgentConfig().getAllowPersonalBuilds();
    }

    public String getSummary()
    {
        return I18N.format("summary", agentRequirements.getSummary());
    }

    public String getUnfulfilledReason(RecipeAssignmentRequest request)
    {
        return I18N.format("unfulfilled.reason", agentRequirements.getUnfulfilledReason(request));
    }

    public boolean isFulfilledBy(RecipeAssignmentRequest request, AgentService service)
    {
        return agentRequirements.isFulfilledBy(request, service) && verifyPersonalBuilds(request, service);
    }
}
