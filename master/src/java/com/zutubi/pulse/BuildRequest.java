/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse;

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
    private String recipeName;

    /**
     * @param projectName
     */
    public BuildRequest(String projectName)
    {
        this.when = new Date(System.currentTimeMillis());
        this.projectName = projectName;
        this.recipeName = null;        
    }
    
    public BuildRequest(String projectName, String recipeName)
    {
        this.when = new Date(System.currentTimeMillis());
        this.projectName = projectName;
        this.recipeName = recipeName;
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
     */
    public String getProjectName()
    {
        return projectName;
    }

    /**
     * The name of the recipe to be built.
     */
    public String getRecipeName()
    {
        return recipeName;
    }
}
