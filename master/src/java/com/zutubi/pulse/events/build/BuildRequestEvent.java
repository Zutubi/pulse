package com.zutubi.pulse.events.build;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.model.BuildReason;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.util.TimeStamps;

/**
 */
public class BuildRequestEvent extends Event
{
    private BuildReason reason;
    private Project project;
    private String specification;
    private long queued;
    private BuildRevision revision;

    public BuildRequestEvent(Object source, BuildReason reason, Project project, String specification, BuildRevision revision)
    {
        super(source);
        this.reason = reason;
        this.project = project;
        this.specification = specification;
        this.revision = revision;
        queued = System.currentTimeMillis();
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

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Build Request Event");
        if (getProject() != null)
        {
            buff.append(": ").append(getProject().getName());
        }
        if (getReason() != null)
        {
            buff.append(": ").append(getReason().getSummary());
        }
        return buff.toString();
    }
}
