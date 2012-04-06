package com.zutubi.pulse.master.model;

import com.zutubi.pulse.core.model.Entity;

/**
 * Links builds with a dependency relationship.
 */
public class BuildDependencyLink extends Entity
{
    private long upstreamBuildId;
    private long downstreamBuildId;

    public BuildDependencyLink()
    {
    }

    public BuildDependencyLink(long upstreamBuildId, long downstreamBuildId)
    {
        this.upstreamBuildId = upstreamBuildId;
        this.downstreamBuildId = downstreamBuildId;
    }

    public long getUpstreamBuildId()
    {
        return upstreamBuildId;
    }

    public void setUpstreamBuildId(long upstreamBuildId)
    {
        this.upstreamBuildId = upstreamBuildId;
    }

    public long getDownstreamBuildId()
    {
        return downstreamBuildId;
    }

    public void setDownstreamBuildId(long downstreamBuildId)
    {
        this.downstreamBuildId = downstreamBuildId;
    }
}
