package com.zutubi.pulse.master.model;

/**
 * Indicates that the build was triggered by a project dependency relationship.
 */
public class DependencyBuildReason extends AbstractBuildReason
{
    private String dependencyName;

    public DependencyBuildReason()
    {
    }

    public DependencyBuildReason(String dependencyName)
    {
        this.dependencyName = dependencyName;
    }

    public String getSummary()
    {
        return String.format("dependency trigger from %s", dependencyName);
    }

    public String getDependencyName()
    {
        return dependencyName;
    }

    private void setDependencyName(String dependencyName)
    {
        this.dependencyName = dependencyName;
    }
}
