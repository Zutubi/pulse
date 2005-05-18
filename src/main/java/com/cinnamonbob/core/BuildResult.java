package com.cinnamonbob.core;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Contains the results for a project build.
 */
public class BuildResult implements Iterable<CommandResultCommon>
{
    private static final Logger LOG = Logger.getLogger(BuildResult.class.getName());
    
    /*
     * This information is transient as it is stored externally.
     */
    private transient String projectName;
    private transient int id;
    private transient List<CommandResultCommon> commandResults;
    
    private FeatureCategoryRegistry categoryRegistry;
    private TimeStamps stamps;
    /**
     * If non-null, contains the details of something very nasty that stopped
     * the build from completing.
     */
    private InternalBuildFailureException internalFailure;
    
    
    /**
     * Creates a new build result for the given project.
     */
    public BuildResult(String projectName, int id, FeatureCategoryRegistry categoryRegistry)
    {
        this.projectName = projectName;
        this.id = id;
        this.commandResults = new LinkedList<CommandResultCommon>();
        this.categoryRegistry = categoryRegistry;
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
    
    
    public FeatureCategory getFeatureCategory(String name)
    {
        return categoryRegistry.getFeatureCategory(name);
    }

    
    public void load(String projectName, int id, File buildDir)
    {
        this.projectName = projectName;
        this.id = id;
        this.commandResults = new LinkedList<CommandResultCommon>();
    }

    
    /**
     * Locates and returns the result for the command of the given name, if
     * available.
     * 
     * @param commandName
     *        the name of the command to tfind the result for
     * @return the result of the command, or null if not found
     */
    public CommandResultCommon getCommandResult(String commandName)
    {
        for(CommandResultCommon result: commandResults)
        {
            if(result.getCommandName().equals(commandName))
            {
                return result;
            }
        }
        
        return null;
    }

    /**
     * Returns true if any commands in the build detected changes by the
     * given login.
     * 
     * @param login
     *        the login to test for changes by
     * @return true iff a change was detected by the given user
     */
    public boolean changedBy(String login)
    {
        for(CommandResultCommon result: commandResults)
        {
            if(result.getResult().changedBy(login))
            {
                return true;
            }
        }
        return false;
    }    
}
