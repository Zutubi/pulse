package com.zutubi.pulse.events.build;

import com.zutubi.events.Event;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;
import com.zutubi.util.TimeStamps;

/**
 * Base class for build requests.  Specific subclasses are used to
 * differentiate project and personal build requests.
 */
public abstract class AbstractBuildRequestEvent extends Event
{
    private BuildRevision revision;
    private long queued;
    protected ProjectConfiguration projectConfig;
    protected String requestSource;
    protected boolean replaceable;

    /**
     * @param source        the event source
     * @param revision      build revision to use for the build, may not be
     *                      initialised if the revision should float
     * @param projectConfig configuration of the project to build, snapshotted
     *                      in time for this entire build
     * @param requestSource the source of the request - requests from the same
     *                      source may replace each other if replaceable is
     *                      true
     * @param replaceable   if true, this request may be replaced by later
     *                      requests with the same request source, provided the
     *                      build has not yet commenced
     */
    public AbstractBuildRequestEvent(Object source, BuildRevision revision, ProjectConfiguration projectConfig, String requestSource, boolean replaceable)
    {
        super(source);
        this.revision = revision;
        this.projectConfig = projectConfig;

        this.queued = System.currentTimeMillis();
        this.requestSource = requestSource;
        this.replaceable = replaceable;
    }

    public abstract Entity getOwner();
    public abstract boolean isPersonal();
    public abstract BuildResult createResult(ProjectManager projectManager, UserManager userManager);

    public BuildRevision getRevision()
    {
        return revision;
    }

    /**
     * Update the revision due to a new request arriving with the same source.
     * Note this request must be replaceable to allow this.
     *
     * @param revision the new revision to use for the build
     * @throws IllegalStateException if this request is not replaceable
     */
    public void setRevision(BuildRevision revision)
    {
        if (!replaceable)
        {
            throw new IllegalStateException("Attempt to update revision for a non-replaceable build request.");
        }

        this.revision = revision;
    }

    public ProjectConfiguration getProjectConfig()
    {
        return projectConfig;
    }

    public long getQueued()
    {
        return queued;
    }

    public String getPrettyQueueTime()
    {
        return TimeStamps.getPrettyTime(queued);
    }

    public String getRequestSource()
    {
        return requestSource;
    }

    public boolean isReplaceable()
    {
        return replaceable;
    }
}
