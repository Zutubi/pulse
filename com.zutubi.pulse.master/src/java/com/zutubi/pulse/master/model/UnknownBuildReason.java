package com.zutubi.pulse.master.model;

/**
 * Used to upgrade build results from old versions.
 */
public class UnknownBuildReason extends AbstractBuildReason
{
    public String getSummary()
    {
        return "unknown";
    }
}
