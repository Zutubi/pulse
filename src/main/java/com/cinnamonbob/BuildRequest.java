package com.cinnamonbob;

/**
 * Contains the details of the build being requested.
 *
 * @author Daniel Ostermeier
 */
public class BuildRequest
{
    private String projectName;

    public BuildRequest(String projectName)
    {
        this.projectName = projectName;
    }

    /**
     * Get the name of the project to be built.
     * @return
     */
    public String getProjectName()
    {
        return projectName;
    }
}
