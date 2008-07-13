package com.zutubi.pulse.events.build;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.util.TimeStamps;

/**
 */
public abstract class AbstractBuildRequestEvent extends Event
{
    private BuildRevision revision;
    private long queued;
    protected ProjectConfiguration projectConfig;

    public AbstractBuildRequestEvent(Object source, BuildRevision revision, ProjectConfiguration projectConfig)
    {
        super(source);
        this.revision = revision;
        this.projectConfig = projectConfig;

        this.queued = System.currentTimeMillis();
    }

    public abstract Entity getOwner();
    public abstract boolean isPersonal();
    public abstract BuildResult createResult(ProjectManager projectManager, UserManager userManager);

    public BuildRevision getRevision()
    {
        return revision;
    }

    public ProjectConfiguration getProjectConfig()
    {
        return projectConfig;
    }

    public long getQueued()
    {
        return queued;
    }

    public String getPrettyQueueTime()
    {
        return TimeStamps.getPrettyTime(queued);
    }
}
