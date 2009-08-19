package com.zutubi.pulse.master.events.build;

import com.zutubi.events.Event;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.model.NamedEntity;
import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.util.TimeStamps;

import java.util.Collection;

/**
 * Base class for build requests.  Specific subclasses are used to
 * differentiate project and personal build requests.
 */
public abstract class BuildRequestEvent extends Event implements Comparable
{
    private BuildRevision revision;
    private long queued;
    protected ProjectConfiguration projectConfig;
    protected TriggerOptions options;

    /**
     * If set, this build id indicates that this build request is
     * associated with an existing build.
     */
    private long buildId;

    /**
     * @param source        the event source
     * @param revision      build revision to use for the build, may not be
     *                      initialised if the revision should float
     * @param projectConfig configuration of the project to build, snapshotted
     *                      in time for this entire build
     * @param options       the options for this build.
     */
    public BuildRequestEvent(Object source, BuildRevision revision, ProjectConfiguration projectConfig, TriggerOptions options)
    {
        super(source);
        this.revision = revision;
        this.projectConfig = projectConfig;
        this.options = options;
        this.queued = System.currentTimeMillis();
        this.buildId = options.getBuildId();
    }

    public abstract NamedEntity getOwner();
    public abstract boolean isPersonal();

    /**
     * Create a new persistent instance of the build result represented by this request.
     *
     * @param projectManager    project manager resource
     * @param buildManager      build manager resource
     *
     * @return a persistent build result instance.
     */
    public abstract BuildResult createResult(ProjectManager projectManager, BuildManager buildManager);

    /**
     * The status for the requested build.
     * 
     * @return  the requested builds status string.
     */
    public abstract String getStatus();

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
        if (!options.isReplaceable())
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

    public TriggerOptions getOptions()
    {
        return options;
    }

    public BuildReason getReason()
    {
        return options.getReason();
    }

    public Collection<ResourcePropertyConfiguration> getProperties()
    {
        return options.getProperties();
    }

    public String getRequestSource()
    {
        return options.getSource();
    }

    public boolean isReplaceable()
    {
        return options.isReplaceable();
    }

    public long getBuildId()
    {
        return buildId;
    }

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public int compareTo(Object o)
    {
        BuildRequestEvent other = (BuildRequestEvent) o;
        if (queued > other.queued)
        {
            return 1;
        }
        else if (queued < other.queued)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
}
