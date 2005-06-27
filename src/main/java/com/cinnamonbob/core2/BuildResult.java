package com.cinnamonbob.core2;

import com.cinnamonbob.core2.config.CommandResult;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class BuildResult
{
    private boolean succeeded;
    private String projectName;
    private int id;
    
    private List<CommandResult> results = new LinkedList<CommandResult>();
    
    public BuildResult()
    {
    }
    
    public void setSucceeded(boolean b)
    {
        succeeded = b;
    }
    
    public boolean succeeded()
    {
        return succeeded;
    }

    public void add(CommandResult result)
    {
        results.add(result);
    }
    
    public List<CommandResult> getCommandResults()
    {
        return results;
    }


    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }
}
