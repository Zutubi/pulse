package com.cinnamonbob;

import com.cinnamonbob.model.BuildResult;

/**
 * This event is raised by the build processor when a build is completed.
 */
public class BuildCompletedEvent extends BuildEvent
{
    public BuildCompletedEvent(Object source, BuildResult result)
    {
        super(source, result);
    }
}
