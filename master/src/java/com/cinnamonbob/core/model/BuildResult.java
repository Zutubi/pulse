package com.cinnamonbob.core.model;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.core.util.TimeStamps;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 */
public class BuildResult extends Result
{
    private static final int MAX_MESSAGE_LENGTH = 1023;
    private static final String EXCEPTION_FILE = "exception";

    private ResultState state = ResultState.INITIAL;
    private String errorMessage;
    private long number;
    private String projectName;
    private TimeStamps stamps;
    private File outputDir;
    private List<CommandResult> results;
    /** Map from SCM id to details for that SCM. */
    private Map<Long, BuildScmDetails> scmDetails;

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

    public long getNumber()
    {
        return number;
    }

    public void setNumber(long number)
    {
        this.number = number;
    }

    public String getExceptionTrace()
    {
        File exceptionFile = new File(outputDir, EXCEPTION_FILE);
        String result = null;

        try
        {
            result = IOUtils.fileToString(exceptionFile);
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

    public Map<Long, BuildScmDetails> getScmDetails()
    {
        if(scmDetails == null)
        {
            scmDetails = new HashMap<Long, BuildScmDetails>();
        }
        return scmDetails;
    }

    public BuildScmDetails getScmDetails(long scmId)
    {
        return scmDetails.get(scmId);
    }

    private void setScmDetails(Map<Long, BuildScmDetails> scmDetails)
    {
        this.scmDetails = scmDetails;
    }

    public void addScmDetails(long scmId, BuildScmDetails details)
    {
        getScmDetails().put(scmId, details);
    }

    public boolean hasChanges()
    {
        for(BuildScmDetails details: scmDetails.values())
        {
            if(details.getRevision() != null || details.getChangelists().size() > 0)
            {
                return true;
            }
        }

        return false;
    }
}
