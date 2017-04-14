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

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.master.agent.AgentService;
import com.zutubi.pulse.master.build.queue.RecipeAssignmentRequest;
import com.zutubi.pulse.master.model.ResourceManager;

/**
 * Requirements that allow a stage to be dispatched to any agent that has the
 * required resources.
 */
public class AnyCapableAgentRequirements implements AgentRequirements
{
    private static final Messages I18N = Messages.getInstance(AnyCapableAgentRequirements.class);

    private ResourceManager resourceManager;

    public String getSummary()
    {
        return I18N.format("summary");
    }

    public boolean isFulfilledBy(RecipeAssignmentRequest request, AgentService service)
    {
        return resourceManager.getAgentRepository(service.getAgentConfig()).satisfies(request.getResourceRequirements());
    }

    public String getUnfulfilledReason(RecipeAssignmentRequest request)
    {
        StringBuffer message = new StringBuffer();
        message.append(I18N.format("unfulfilled.reason"));
        String sep = " ";
        for (ResourceRequirement requirement : request.getResourceRequirements())
        {
            if (!requirement.isOptional())
            {
                message.append(sep);
                message.append(requirement);
                sep = ", ";
            }
        }
        message.append(".");
        return message.toString();
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }
}
