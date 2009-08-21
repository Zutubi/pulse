package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.core.PulseExecutionContext;

/**
 * This event is raised by a meta build handler when it has finished handling
 * the build.
 */
public class MetaBuildCompletedEvent extends BuildEvent
{
    public MetaBuildCompletedEvent(Object source, BuildResult result, PulseExecutionContext context)
    {
        super(source, result, context);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Meta Build Completed Event");
        if (getBuildResult() != null)
        {
            buff.append(": ").append(getBuildResult().getMetaBuildId());
        }
        return buff.toString();
    }
}
