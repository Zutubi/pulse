package com.cinnamonbob;

import java.util.Date;

/**
 * Contains the details of the build being requested.
 *
 * @author Daniel Ostermeier
 */
public class BuildRequest
{
    private Date   when;
    private String projectName;

    /**
     * @param projectName
     */
    public BuildRequest(String projectName)
    {
        this.when = new Date(System.currentTimeMillis());
        this.projectName = projectName;
    }

    /**
     * @return the time when this request was issued
     */
    public Date getWhen()
    {
        return when;
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
