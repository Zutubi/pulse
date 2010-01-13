package com.zutubi.pulse.master.model;

/**
 * The build reason for any downstream build that is triggered in response to
 * a project dependency relationship.
 */
public class DependencyBuildReason extends AbstractBuildReason
{
    /**
     * The name of the upstream project that is the source of this build request.
     */
    private String source;

    public DependencyBuildReason()
    {
    }

    public DependencyBuildReason(String source)
    {
        this.source = source;
    }

    public String getSummary()
    {
        return "build of dependent of "+ source;
    }

    // for hibernate only.
    private String getSource()
    {
        return source;
    }

    // for hibernate only.
    private void setSource(String source)
    {
        this.source = source;
    }
}
