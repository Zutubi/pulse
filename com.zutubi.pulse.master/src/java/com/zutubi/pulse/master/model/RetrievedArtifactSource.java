package com.zutubi.pulse.master.model;

import com.zutubi.util.Sort;

import java.util.Comparator;

/**
 * A struct that holds information about an artifact retrieved by a stage, broken down by the
 * project, build and stage the artifact was retrieved from.
 */
public class RetrievedArtifactSource implements Comparable<RetrievedArtifactSource>
{
    private static final Comparator<String> STRING_COMPARATOR = new Sort.StringComparator();

    private String projectName;
    private String projectUrl;
    private long buildNumber = 0;
    private String buildUrl;
    private String stageName;
    private String artifactName;
    private String artifactUrl;

    /**
     * @return the name of the project the artifact was retrieved from
     */
    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    /**
     * @return URL of the project in the Pulse UI, or null if the project no longer exists
     */
    public String getProjectUrl()
    {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl)
    {
        this.projectUrl = projectUrl;
    }

    /**
     * @return true iff the number of the build that the artifact was retrieved from is known
     */
    public boolean hasBuildNumber()
    {
        return buildNumber > 0;
    }

    /**
     * @return number of the build the artifact was retrieved from
     */
    public long getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(long buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    /**
     * @return URL of the build in the Pulse UI, or null if the build could not be determined or found
     */
    public String getBuildUrl()
    {
        return buildUrl;
    }

    public void setBuildUrl(String buildUrl)
    {
        this.buildUrl = buildUrl;
    }

    /**
     * @return name of the stage the artifact was retrieved from
     */
    public String getStageName()
    {
        return stageName;
    }

    public void setStageName(String stageName)
    {
        this.stageName = stageName;
    }

    /**
     * @return name of the retrieved artifact
     */
    public String getArtifactName()
    {
        return artifactName;
    }

    public void setArtifactName(String artifactName)
    {
        this.artifactName = artifactName;
    }

    /**
     * @return URL of the retrieved artifact in the Pulse UI, or null if the artifact could not be
     *         determined or found
     */
    public String getArtifactUrl()
    {
        return artifactUrl;
    }

    public void setArtifactUrl(String artifactUrl)
    {
        this.artifactUrl = artifactUrl;
    }

    public int compareTo(RetrievedArtifactSource o)
    {
        int comparison = STRING_COMPARATOR.compare(projectName, o.projectName);
        if (comparison != 0)
        {
            return comparison;
        }

        comparison = STRING_COMPARATOR.compare(stageName, o.stageName);
        if (comparison != 0)
        {
            return comparison;
        }

        return STRING_COMPARATOR.compare(artifactName, o.artifactName);
    }
}
