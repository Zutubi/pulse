package com.cinnamonbob.core.model;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.core.util.TimeStamps;

import java.io.*;

public abstract class Result extends Entity
{
    private static final int MAX_MESSAGE_LENGTH = 1023;
    private static final String EXCEPTION_FILE = "exception";

    protected ResultState state = ResultState.INITIAL;
    protected TimeStamps stamps;
    protected File outputDir;
    protected String errorMessage;

    public boolean inProgress()
    {
        return ResultState.IN_PROGRESS == getState();
    }

    public boolean succeeded()
    {
        return ResultState.SUCCESS == getState();
    }

    public boolean failed()
    {
        return ResultState.FAILURE == getState();
    }

    public boolean errored()
    {
        return ResultState.ERROR == getState();
    }

    public boolean commenced()
    {
        return inProgress() || completed();
    }

    public boolean completed()
    {
        return succeeded() || errored() || failed();
    }

    public void commence(File outputDir)
    {
        this.outputDir = outputDir;
        state = ResultState.IN_PROGRESS;
        stamps = new TimeStamps();
    }

    public void complete()
    {
        if (state == ResultState.IN_PROGRESS)
        {
            // Phew, nothing went wrong.
            state = ResultState.SUCCESS;
        }

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

        if (errorMessage.length() > MAX_MESSAGE_LENGTH)
        {
            errorMessage = errorMessage.substring(0, MAX_MESSAGE_LENGTH);
        }

        if (outputDir != null)
        {
            try
            {
                e.printStackTrace(new PrintStream(new FileOutputStream(new File(outputDir, EXCEPTION_FILE)), true));
            }
            catch (FileNotFoundException ignored)
            {
                // no need to handle this, we did our level best
            }
        }
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

    public TimeStamps getStamps()
    {
        return stamps;
    }

    private void setStamps(TimeStamps stamps)
    {
        this.stamps = stamps;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    private void setErrorMessage(String message)
    {
        errorMessage = message;
    }

    public String getExceptionTrace()
    {
        File exceptionFile = new File(outputDir, EXCEPTION_FILE);
        String result = null;

        try
        {
            result = IOUtils.fileToString(exceptionFile);
        }
        catch (IOException e)
        {
            // Empty
        }

        return result;
    }

    private String getOutputDir()
    {
        if (outputDir == null)
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
        if (dir == null)
        {
            outputDir = null;
        }
        else
        {
            outputDir = new File(dir);
        }
    }
}
