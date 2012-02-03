package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.resources.api.ResourcePropertyConfiguration;
import com.zutubi.tove.config.Undefined;
import com.zutubi.util.StringUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A value object for carrying the options for triggering a build.
 */
public class TriggerOptions
{
    /**
     * Additional properties introduced into the build context just
     * after the project properties.
     */
    private Collection<ResourcePropertyConfiguration> properties = new LinkedList<ResourcePropertyConfiguration>();

    /**
     * The reason the build was triggered.
     */
    private BuildReason reason;

    /**
     * A freeform source for the trigger, used to identify related
     * triggers for replacing.
     */
    private String source;

    /**
     * If true, while queue this build request may be replaced by
     * another with the same source.
     */
    private boolean replaceable = false;

    /**
     * If true, force a build to occur even if the latest has been built.
     * Defaults to true.
     */
    private boolean force = true;

    /**
     * The dependency status associated with this build request.
     */
    private String status = null;

    /**
     * The version string to be used for the build request.
     */
    private String version;

    /**
     * Indicates whether or not the version string needs to have any variables
     * resolved.  This will be true for all new builds, but false if the version
     * is being cascaded from a dependent build.
     */
    private boolean resolveVersion = true;

    /**
     * Indicates whether or not this is a request to rebuild this project and
     * all of its dependencies.
     */
    private boolean rebuild = false;

    /**
     * Indicates whether or not we can allow this build to jump the queue.  Builds that
     * can jump the queue allow for optimising the build runtime by jumping past builds
     * that are waiting on other builds. 
     */
    private boolean jumpQueueAllowed = true;

    private int priority = Undefined.INTEGER;

    public TriggerOptions(TriggerOptions other)
    {
        this.properties.addAll(other.properties);
        this.reason = other.reason;
        this.source = other.source;
        this.replaceable = other.replaceable;
        this.force = other.force;
        this.status = other.status;
        this.version = other.version;
        this.resolveVersion = other.resolveVersion;
        this.rebuild = other.rebuild;
        this.jumpQueueAllowed = other.jumpQueueAllowed;
        this.priority = other.priority;
    }

    public TriggerOptions(BuildReason reason, String source)
    {
        this.reason = reason;
        this.source = source;
    }

    public TriggerOptions(BuildReason reason, List<ResourcePropertyConfiguration> properties)
    {
        this.reason = reason;
        this.properties = properties;
    }

    public void setProperties(Collection<ResourcePropertyConfiguration> properties)
    {
        this.properties = properties;
    }

    public void addProperties(Collection<ResourcePropertyConfiguration> properties)
    {
        properties.addAll(properties);
    }

    public Collection<ResourcePropertyConfiguration> getProperties()
    {
        return properties;
    }

    public void setForce(boolean b)
    {
        this.force = b;
    }

    public boolean isForce()
    {
        return force;
    }

    public void setReplaceable(boolean b)
    {
        this.replaceable = b;
    }

    public boolean isReplaceable()
    {
        return replaceable;
    }

    public BuildReason getReason()
    {
        return reason;
    }

    public void setReason(BuildReason reason)
    {
        this.reason = reason;
    }

    public String getSource()
    {
        return source;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public boolean hasStatus()
    {
        return StringUtils.stringSet(this.status);
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public boolean hasVersion()
    {
        return StringUtils.stringSet(this.version);
    }

    public boolean isResolveVersion()
    {
        return resolveVersion;
    }

    public void setResolveVersion(boolean resolveVersion)
    {
        this.resolveVersion = resolveVersion;
    }

    public boolean isRebuild()
    {
        return rebuild;
    }

    public void setRebuild(boolean rebuild)
    {
        this.rebuild = rebuild;
    }

    public boolean isJumpQueueAllowed()
    {
        return jumpQueueAllowed;
    }

    public void setJumpQueueAllowed(boolean b)
    {
        this.jumpQueueAllowed = b;
    }

    public boolean hasPriority()
    {
        return this.priority != Undefined.INTEGER;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }
}
