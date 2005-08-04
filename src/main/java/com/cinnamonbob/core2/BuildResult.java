package com.cinnamonbob.core2;

import com.cinnamonbob.model.CommandResult;
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
    public enum BuildState 
    {
        INITIAL,
        BUILDING,
        COMPLETED
    }
    
    private BuildState state;
    private long number;
    private boolean succeeded;
    private String projectName;
    private String revision;
    private TimeStamps stamps;
    
    private List<CommandResult> results;
    
    public BuildResult()
    {
    }
    
    public BuildResult(String projectName, long number)
    {
        this.number = number;
        this.projectName = projectName;
        state = BuildState.INITIAL;
        results = new LinkedList<CommandResult>();
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

    private void setCommandResults(List<CommandResult> results)
    {
        this.results = results;
    }
    
    public String getProjectName()
    {
        return projectName;
    }

    private void setProjectName(String projectName)
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
         
    public void building()
    {
        state = BuildState.BUILDING;
        stamps = new TimeStamps();
    }
    public void completed()
    {
        state = BuildState.COMPLETED;
        stamps.end();
    }

    public BuildState getState()
    {
        return state;
    }

    private void setState(BuildState state)
    {
        this.state = state;
    }
    
    private String getStateName()
    {
        return state.name();
    }
    
    private void setStateName(String name)
    {
        state = BuildState.valueOf(name);
    }

    public String getRevision()
    {
        return revision;
    }

    public void setRevision(String revision)
    {
        this.revision = revision;
    }

    public long getNumber()
    {
        return number;
    }

    public void setNumber(long number)
    {
        this.number = number;
    }
}
