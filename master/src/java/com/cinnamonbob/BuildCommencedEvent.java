package com.cinnamonbob;

import com.cinnamonbob.model.BuildResult;

/**
 * This event is raised by the build processor when commencing a build.
 */
public class BuildCommencedEvent extends BuildEvent
{
    public BuildCommencedEvent(Object source, BuildResult result)
    {
        super(source, result);
    }
}
