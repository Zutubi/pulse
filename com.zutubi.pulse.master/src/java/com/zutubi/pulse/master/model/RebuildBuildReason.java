package com.zutubi.pulse.master.model;

/**
 * The build reason for any upstream build that is triggered in response to
 * a project dependency relationship.
 */
public class RebuildBuildReason extends AbstractBuildReason
{
    /**
     * The build reason of the original build request, referenced by
     * this build reasons summary.
     */
    private String sourceBuildReason;

    public RebuildBuildReason()
    {
    }

    public RebuildBuildReason(String sourceBuildReason)
    {
        this.sourceBuildReason = sourceBuildReason;
    }

    public String getSummary()
    {
        return "rebuild triggered by ("+sourceBuildReason+")";
    }

    // for hibernate only.
    private String getSourceBuildReason()
    {
        return sourceBuildReason;
    }

    // for hibernate only.
    private void setSourceBuildReason(String sourceBuildReason)
    {
        this.sourceBuildReason = sourceBuildReason;
    }
}
