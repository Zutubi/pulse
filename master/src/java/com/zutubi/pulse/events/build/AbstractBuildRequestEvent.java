package com.zutubi.pulse.events.build;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.util.TimeStamps;

/**
 */
public abstract class AbstractBuildRequestEvent extends Event
{
    private BuildRevision revision;
    private Project project;
    private BuildSpecification specification;
    private long queued;

    public AbstractBuildRequestEvent(Object source, BuildRevision revision, Project project, BuildSpecification specification)
    {
        super(source);
        this.revision = revision;
        this.project = project;
        this.specification = specification;

        this.queued = System.currentTimeMillis();
    }

    public abstract Entity getOwner();
    public abstract boolean isPersonal();
    public abstract BuildResult createResult(ProjectManager projectManager, UserManager userManager);

    public BuildRevision getRevision()
    {
        return revision;
    }

    public Project getProject()
    {
        return project;
    }

    public BuildSpecification getSpecification()
    {
        return specification;
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
