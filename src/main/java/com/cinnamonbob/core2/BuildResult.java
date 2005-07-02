package com.cinnamonbob.core2;

import com.cinnamonbob.core2.config.CommandResult;
import com.cinnamonbob.model.Entity;
import com.cinnamonbob.util.TimeStamps;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class BuildResult extends Entity
{
    private boolean succeeded;
    private String projectName;
    private TimeStamps stamps;
    
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
    
    /*
     * This should keep hibernate happy.
     */
    private boolean isSucceeded()
    {
        return succeeded();
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

    public TimeStamps getStamps()
    {
        return stamps;
    }
    
    public void setStamps(TimeStamps stamps)
    {
        this.stamps = stamps;
    }
}
