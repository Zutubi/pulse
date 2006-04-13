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
