package com.cinnamonbob;

/**
 * @author Daniel Ostermeier
 */
public class BuildRequest
{
    private String projectName;

    public BuildRequest(String projectName)
    {
        this.projectName = projectName;
    }

    public String getProjectName()
    {
        return projectName;
    }
}
