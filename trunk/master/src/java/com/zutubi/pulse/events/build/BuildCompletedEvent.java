package com.zutubi.pulse.events.build;

import com.zutubi.pulse.model.BuildResult;

/**
 * This event is raised by the build processor when a build is completed.
 */
public class BuildCompletedEvent extends BuildEvent
{
    public BuildCompletedEvent(Object source, BuildResult result)
    {
        super(source, result);
    }

    public String toString()
    {
        StringBuffer buff = new StringBuffer("Build Completed Event");
        if (getResult() != null)
        {
            buff.append(": ").append(getResult().getId());
        }
        return buff.toString();
    }    
}
