package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.model.BuildHostRequirements;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.util.TimeStamps;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;

/**
 * A request to dispatch a recipe to some build hostRequirements, which may be restricted.
 */
public class RecipeDispatchRequest
{
    private BuildHostRequirements hostRequirements;
    private BuildRevision revision;
    private RecipeRequest request;
    private ProjectConfiguration projectConfig;
    private BuildResult build;
    private long queueTime;
    /**
     * Time at which the request should be timed out as no capable agent has
     * become available.  If negative, no timeout is currently in place.
     */
    private long timeout = -1;

    public RecipeDispatchRequest(BuildHostRequirements hostRequirements, BuildRevision revision, RecipeRequest request, ProjectConfiguration projectConfig, BuildResult build)
    {
        this.hostRequirements = hostRequirements;
        this.revision = revision;
        this.request = request;
        this.build = build;
        this.projectConfig = projectConfig;
    }

    public BuildHostRequirements getHostRequirements()
    {
        return hostRequirements;
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

    public ProjectConfiguration getProjectConfig()
    {
        return projectConfig;
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
}
