package com.zutubi.pulse.events.build;

import com.zutubi.pulse.util.TimeStamps;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.BuildReason;
import com.zutubi.pulse.core.BuildRevision;

/**
 */
public class BuildRequestEvent extends Event
{
    private BuildReason reason;
    private Project project;
    private String specification;
    private long queued;
    private BuildRevision revision;

    public BuildRequestEvent(Object source, BuildReason reason, Project project, String specification)
    {
        super(source);
        this.reason = reason;
        this.project = project;
        this.specification = specification;
        queued = System.currentTimeMillis();
        revision = new BuildRevision();
    }

    public BuildReason getReason()
    {
        return reason;
    }

    public Project getProject()
    {
        return project;
    }

    public String getSpecification()
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

    public BuildRevision getRevision()
    {
        return revision;
    }
}
