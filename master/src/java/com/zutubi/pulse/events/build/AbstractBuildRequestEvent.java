package com.zutubi.pulse.events.build;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.util.TimeStamps;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;

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

    public abstract Object getOwner();
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
