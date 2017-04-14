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

package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.resources.ResourceRequirement;
import com.zutubi.pulse.master.agent.Agent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.AgentRequirements;
import com.zutubi.util.time.TimeStamps;

import java.util.List;

/**
 * A request to dispatch a recipe to some build hostRequirements, which may be restricted.
 */
public class RecipeAssignmentRequest
{
    private static final int UNDEFINED = -1;

    /**
     * The default recipe assignment priority.
     */
    private static final int DEFAULT_PRIORITY = 0;

    private Project project;
    private AgentRequirements hostRequirements;
    private List<ResourceRequirement> resourceRequirements;
    private RecipeRequest request;
    private BuildResult build;
    private long queueTime;

    /**
     * The priority defines the order in which the recipe assignment process
     * will review requests.
     *
     * Given that an agent fulfills the requirements for two recipes, the request
     * with the higher priority will be assigned first.  If both priorities are
     * the same, then the request that was created first will be assigned.
     */
    private int priority = DEFAULT_PRIORITY;

    /**
     * Time at which the request should be timed out as no capable agent has
     * become available.  If negative, no timeout is currently in place.
     */
    private long timeout = UNDEFINED;

    public RecipeAssignmentRequest(Project project, AgentRequirements hostRequirements, List<ResourceRequirement> resourceRequirements, RecipeRequest request, BuildResult build)
    {
        this.project = project;
        this.hostRequirements = hostRequirements;
        this.resourceRequirements = resourceRequirements;
        this.request = request;
        this.build = build;
    }

    public boolean isFulfilledBy(Agent agent)
    {
        return getHostRequirements().isFulfilledBy(this, agent.getService());
    }

    public String getUnfulfilledReason()
    {
        return getHostRequirements().getUnfulfilledReason(this);
    }

    public Project getProject()
    {
        return project;
    }

    public AgentRequirements getHostRequirements()
    {
        return hostRequirements;
    }

    public List<ResourceRequirement> getResourceRequirements()
    {
        return resourceRequirements;
    }

    public RecipeRequest getRequest()
    {
        return request;
    }

    public BuildResult getBuild()
    {
        return build;
    }

    public void queued(long time)
    {
        queueTime = time;
    }

    public long getQueueTime()
    {
        return queueTime;
    }

    public String getPrettyQueueTime()
    {
        return TimeStamps.getPrettyTime(queueTime);
    }

    public long getTimeout()
    {
        return timeout;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public boolean hasTimeout()
    {
        return timeout >= 0;
    }

    public boolean hasTimedOut(long currentTime)
    {
        return hasTimeout() && currentTime >= timeout;
    }

    public void clearTimeout()
    {
        timeout = UNDEFINED;
    }

    public boolean isPersonal()
    {
        return build.isPersonal();
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }
}
