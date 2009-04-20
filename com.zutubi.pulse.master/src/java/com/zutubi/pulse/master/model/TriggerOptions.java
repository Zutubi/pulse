package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.scm.api.Revision;

import java.util.Collection;
import java.util.LinkedList;

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
    private final BuildReason reason;

    /**
     * The revision to build, or null if the revision is not fixed
     * (in which case changelist isolation may result in multiple build
     * requests).
     */
    private Revision revision;

    /**
     * A freeform source for the trigger, used to identify related
     * triggers for replacing.
     */
    private final String source;

    /**
     * If true, while queue this build request may be replaced by
     * another with the same source(has no effect if isolating changelists).
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

    public TriggerOptions(TriggerOptions other)
    {
        this.properties.addAll(other.properties);
        this.reason = other.reason;
        this.source = other.source;
        this.replaceable = other.replaceable;
        this.force = other.force;
        this.status = other.status;
    }

    public TriggerOptions(BuildReason reason, String source)
    {
        this.reason = reason;
        this.source = source;
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
        return this.status != null;
    }
}
