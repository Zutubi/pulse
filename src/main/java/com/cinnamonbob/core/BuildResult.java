package com.cinnamonbob.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains the results for a project build.
 */
public class BuildResult implements Iterable<CommandResultCommon>
{
    /*
     * This information is transient as it is stored externally.
     */
    private transient String projectName;
    private transient int id;
    private transient List<CommandResultCommon> commandResults;
    
    private TimeStamps stamps;
    /**
     * If non-null, contains the details of something very nasty that stopped
     * the build from completing.
     */
    private InternalBuildFailureException internalFailure;
    

    /**
     * Creates a new build result for the given project.
     */
    public BuildResult(String projectName, int id)
    {
        this.projectName = projectName;
        this.id = id;
        this.commandResults = new LinkedList<CommandResultCommon>();
    }
    
    
    /**
     * True iff the build was successful.
     * 
     * @return true iff the build succeeded
     */
    public boolean succeeded()
    {
        if(internalFailure != null)
        {
            return false;
        }
        
        for(CommandResultCommon result: commandResults)
        {
            if(!result.getResult().succeeded())
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns a summary of the build.
     * 
     * @return a summary of the build result
     */
    public String getSummary()
    {
        StringBuffer summary = new StringBuffer();
        
        for(CommandResultCommon result: commandResults)
        {
            summary.append(result.getResult().getSummary());
            summary.append('\n');
        }
        
        return summary.toString();
    }

    
    public String getProjectName()
    {
        return projectName;
    }
    
    
    public int getId()
    {
        return id;
    }
    
    
    public void addCommandResult(CommandResultCommon commandResult)
    {
        commandResults.add(commandResult);
    }

    
    public void stamp(TimeStamps stamps)
    {
        this.stamps = stamps;
    }
    
    
    public TimeStamps getStamps()
    {
        return stamps;
    }


    public void setInternalFailure(InternalBuildFailureException e)
    {
        internalFailure = e;
    }
    
    
    public InternalBuildFailureException getInternalFailure()
    {
        return internalFailure;
    }


    public Iterator<CommandResultCommon> iterator()
    {
        return commandResults.iterator();
    }
}
