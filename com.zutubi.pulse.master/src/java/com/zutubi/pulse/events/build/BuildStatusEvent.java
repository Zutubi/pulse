package com.zutubi.pulse.events.build;

import com.zutubi.pulse.model.BuildResult;

/**
 * Event with a custom status message for a build.
 */
public class BuildStatusEvent extends BuildEvent
{
    protected String message;

    public BuildStatusEvent(Object source, BuildResult buildResult, String message)
    {
        super(source, buildResult, null);
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public String toString()
    {
        return "Build Status Event " + getId() + ": " + message;
    }
}
