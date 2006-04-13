/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.events.build;

import com.zutubi.pulse.model.BuildResult;

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
