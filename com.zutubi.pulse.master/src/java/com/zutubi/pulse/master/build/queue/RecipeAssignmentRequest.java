package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.AgentRequirements;
import com.zutubi.util.TimeStamps;

import java.util.List;

/**
 * A request to dispatch a recipe to some build hostRequirements, which may be restricted.
 */
public class RecipeAssignmentRequest
{
    private Project project;
    private AgentRequirements hostRequirements;
    private List<ResourceRequirement> resourceRequirements;
    private BuildRevision revision;
    private RecipeRequest request;
    private BuildResult build;
    private long queueTime;
    /**
     * Time at which the request should be timed out as no capable agent has
     * become available.  If negative, no timeout is currently in place.
     */
    private long timeout = -1;

    public RecipeAssignmentRequest(Project project, AgentRequirements hostRequirements, List<ResourceRequirement> resourceRequirements, BuildRevision revision, RecipeRequest request, BuildResult build)
    {
        this.project = project;
        this.hostRequirements = hostRequirements;
        this.resourceRequirements = resourceRequirements;
        this.revision = revision;
        this.request = request;
        this.build = build;
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

    public BuildRevision getRevision()
    {
        return revision;
    }

    public BuildResult getBuild()
    {
        return build;
    }

    public void queued()
    {
        queueTime = System.currentTimeMillis();
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
        timeout = -1;
    }

    public boolean isPersonal()
    {
        return build.isPersonal();
    }
}
