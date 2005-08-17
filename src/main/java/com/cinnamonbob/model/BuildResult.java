package com.cinnamonbob.model;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.util.IOHelper;
import com.cinnamonbob.util.TimeStamps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class BuildResult extends Result
{
    private static final int MAX_MESSAGE_LENGTH = 1023;
    private static final String EXCEPTION_FILE = "exception";
    
    private ResultState state;
    private String errorMessage;
    private long number;
    private String projectName;
    private Revision revision;
    private TimeStamps stamps;
    private File outputDir;
    private List<CommandResult> results;
    private List<Changelist> changelists;

    public BuildResult()
    {
    }
    
    public BuildResult(String projectName, long number)
    {
        this.number = number;
        this.projectName = projectName;
        state = ResultState.INITIAL;
        results = new LinkedList<CommandResult>();
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
    
    private void setStamps(TimeStamps stamps)
    {
        this.stamps = stamps;
    }
         
    public void commence(File outputDir)
    {
        this.outputDir = outputDir;
        state = ResultState.IN_PROGRESS;
        stamps = new TimeStamps();
    }
    
    public void failure()
    {
        state = ResultState.FAILURE;
    }
    
    public void commandError()
    {
        state = ResultState.ERROR;
    }
    
    public void error(BuildException e)
    {
        state = ResultState.ERROR;
        errorMessage = e.getMessage();
        
        if(errorMessage.length() > MAX_MESSAGE_LENGTH)
        {
            errorMessage = errorMessage.substring(0, MAX_MESSAGE_LENGTH);
        }
        
        if(outputDir != null)
        {
            try
            {
                e.printStackTrace(new PrintStream(new FileOutputStream(new File(outputDir, EXCEPTION_FILE)), true));
            }
            catch(FileNotFoundException ignored)
            {
                // no need to handle this, we did our level best
            }
        }
    }
    
    public void complete()
    {
        if(state == ResultState.IN_PROGRESS)
        {
            // Phew, nothing went wrong.
            state = ResultState.SUCCESS;
        }
        
        stamps.end();
    }

    public ResultState getState()
    {
        return state;
    }

    private void setState(ResultState state)
    {
        this.state = state;
    }
    
    public String getStateName()
    {
        return state.name();
    }
    
    private void setStateName(String name)
    {
        state = ResultState.valueOf(name);
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }
    
    private void setErrorMessage(String message)
    {
        errorMessage = message;
    }

    public Revision getRevision()
    {
        return revision;
    }

    public void setRevision(Revision revision)
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

    public void add(Changelist changelist)
    {
        getChangelists().add(changelist);
    }

    public List<Changelist> getChangelists()
    {
        if (changelists == null)
        {
            changelists = new LinkedList<Changelist>();
        }
        return changelists;
    }

    public void setChangelists(List<Changelist> changelists)
    {
        this.changelists = changelists;
    }
    
    public String getExceptionTrace()
    {
        File exceptionFile = new File(outputDir, EXCEPTION_FILE);
        String result = null;
        
        try
        {
            result = IOHelper.fileToString(exceptionFile);
        }
        catch(IOException e)
        {
        }
        
        return result;
    }

    private String getOutputDir()
    {
        if(outputDir == null)
        {
            return null;
        }
        else
        {
            return outputDir.getAbsolutePath();
        }
    }

    private void setOutputDir(String dir)
    {
        if(dir == null)
        {
            outputDir = null;
        }
        else
        {
            outputDir = new File(dir);
        }
    }
}
