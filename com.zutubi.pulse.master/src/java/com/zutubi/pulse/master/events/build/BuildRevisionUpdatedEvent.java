package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.model.BuildResult;

/**
 * Raised when an active build has its revision updated (due to a newer build
 * request replacing the existing one).
 */
public class BuildRevisionUpdatedEvent extends BuildEvent
{
    private BuildRevision buildRevision;

    /**
     * @param source        source of the event
     * @param buildResult   result for the build that has had its revision
     *                      updated
     * @param buildRevision the build revision that has been updated
     */
    public BuildRevisionUpdatedEvent(Object source, BuildResult buildResult, BuildRevision buildRevision)
    {
        super(source, buildResult, null);
        this.buildRevision = buildRevision;
    }

    public BuildRevision getBuildRevision()
    {
        return buildRevision;
    }

    public String toString()
    {
        return "Build Revision Updated Event " + getId();
    }
}
