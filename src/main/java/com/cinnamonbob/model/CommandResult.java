package com.cinnamonbob.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.util.IOHelper;
import com.cinnamonbob.util.TimeStamps;

/**
 * 
 *
 */
public class CommandResult extends Result
{
    private static final int MAX_MESSAGE_LENGTH = 1023;
    private static final String EXCEPTION_FILE = "exception";
    
    private String commandName;
    private TimeStamps stamps;
    private ResultState state;
    private String errorMessage;
    private Properties properties;
    private List<StoredArtifact> artifacts =  new LinkedList<StoredArtifact>();
    private File outputDir;

    protected CommandResult()
    {
        
    }
    
    public CommandResult(String name)
    {
        commandName = name;
        state = ResultState.INITIAL;
    }
        
    public String getCommandName()
    {
        return commandName;
    }

    private void setCommandName(String name)
    {
        this.commandName = name;
    }
    
    public TimeStamps getStamps()
    {
        return stamps;
    }
    
    private void setStamps(TimeStamps stamps)
    {
        this.stamps = stamps;
    }
    
    public ResultState getState()
    {
        return state;
    }

    public void commence(File outputDir)
    {
        this.outputDir = outputDir;
        state = ResultState.IN_PROGRESS;
        stamps = new TimeStamps();
    }

    public void complete()
    {
        stamps.end();
    }
    
    public void success()
    {
        state = ResultState.SUCCESS;
    }
    
    public void failure()
    {
        state = ResultState.FAILURE;
    }
    
    public void error(BuildException e)
    {
        state = ResultState.ERROR;
        errorMessage = e.getMessage();
        
        if(errorMessage.length() > MAX_MESSAGE_LENGTH)
        {
            errorMessage = errorMessage.substring(0, MAX_MESSAGE_LENGTH);
        }
        
        try
        {
            e.printStackTrace(new PrintStream(new FileOutputStream(new File(outputDir, EXCEPTION_FILE)), true));
        }
        catch(FileNotFoundException ignored)
        {
            // no need to handle this, we did our level best
        }
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }
    
    private void setErrorMessage(String message)
    {
        errorMessage = message;
    }
        
    private String getStateName()
    {
        return state.toString();
    }
    
    private void setStateName(String name)
    {
        this.state = ResultState.valueOf(name);
    }

    public void addArtifact(StoredArtifact artifact)
    {
        artifacts.add(artifact);
    }
    
    public StoredArtifact getArtifact(String name)
    {
        for(StoredArtifact a: artifacts)
        {
            if(a.getName().equals(name))
            {
                return a;
            }
        }
        
        return null;
    }
    
    public List<StoredArtifact> getArtifacts()
    {
        return artifacts;
    }
    
    private void setArtifacts(List<StoredArtifact> artifacts)
    {
        this.artifacts = artifacts;
    }
    
    public Properties getProperties()
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        return properties;
    }
    
    private void setProperties(Properties properties)
    {
        this.properties = properties;
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
        }    }

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
}
