package com.cinnamonbob.events.build;

import com.cinnamonbob.core.util.TimeStamps;
import com.cinnamonbob.events.Event;
import com.cinnamonbob.model.Project;

/**
 */
public class BuildRequestEvent extends Event
{
    private Project project;
    private String specification;
    private long queued;

    public BuildRequestEvent(Object source, Project project, String specification)
    {
        super(source);
        this.project = project;
        this.specification = specification;
        queued = System.currentTimeMillis();
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
}
