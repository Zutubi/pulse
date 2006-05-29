/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.events.build;

import com.zutubi.pulse.util.TimeStamps;
import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.model.Project;

/**
 */
public class BuildRequestEvent extends Event
{
    private Project project;

    /**
     * Only set once the bui
     */
    private long specId;

    private long queued;

    public BuildRequestEvent(Object source, Project project, long specId)
    {
        super(source);
        this.project = project;
        this.specId = specId;
        queued = System.currentTimeMillis();
    }

    public Project getProject()
    {
        return project;
    }

    public long getSpecification()
    {
        return specId;
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
