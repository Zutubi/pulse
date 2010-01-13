package com.zutubi.pulse.master.model;

/**
 * The build reason for any upstream build that is triggered in response to
 * a project dependency relationship.
 */
public class RebuildBuildReason extends AbstractBuildReason
{
    /**
     * The name of the downstream project that is the source of this build request.
     */
    private String source;

    public RebuildBuildReason()
    {
    }

    public RebuildBuildReason(String source)
    {
        this.source = source;
    }

    public String getSummary()
    {
        return "build with dependencies of "+ source;
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
