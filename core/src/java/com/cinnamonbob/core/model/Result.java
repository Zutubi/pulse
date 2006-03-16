package com.cinnamonbob.core.model;

import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.core.util.TimeStamps;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public abstract class Result extends Entity
{
    private static final int MAX_MESSAGE_LENGTH = 1023;
    private static final String EXCEPTION_FILE = "exception";

    protected ResultState state = ResultState.INITIAL;
    protected TimeStamps stamps = new TimeStamps();
    protected File outputDir;
    protected String errorMessage;
    protected String failureMessage;

    public boolean pending()
    {
        return ResultState.INITIAL == getState();
    }

    public boolean inProgress()
    {
        return ResultState.IN_PROGRESS == getState();
    }

    public boolean terminating()
    {
        return ResultState.TERMINATING == getState();
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
        return inProgress() || terminating() || completed();
    }

    public boolean completed()
    {
        return succeeded() || errored() || failed();
    }

    public void queue(File outputDir)
    {
        this.outputDir = outputDir;
        stamps.setQueueTime(System.currentTimeMillis());
    }

    public void commence(File outputDir)
    {
        this.outputDir = outputDir;
        state = ResultState.IN_PROGRESS;
        stamps = new TimeStamps();
        stamps.setStartTime(System.currentTimeMillis());
    }

    public void commence(long startTime)
    {
        stamps.setStartTime(startTime);
        // Special case: marked as terminating before we commenced.
        if (state != ResultState.TERMINATING)
        {
            state = ResultState.IN_PROGRESS;
        }
    }

    public void complete()
    {
        if (state == ResultState.IN_PROGRESS)
        {
            // Phew, nothing went wrong.
            state = ResultState.SUCCESS;
        }
        else if (state == ResultState.TERMINATING)
        {
            state = ResultState.ERROR;
        }

        if (stamps.started())
        {
            stamps.end();
        }
    }

    public void success()
    {
        state = ResultState.SUCCESS;
    }

    public void failure()
    {
        state = ResultState.FAILURE;
    }

    public void failure(String message)
    {
        failure();
        failureMessage = message;

        if (failureMessage.length() > MAX_MESSAGE_LENGTH)
        {
            failureMessage = failureMessage.substring(0, MAX_MESSAGE_LENGTH);
        }
    }

    public void error(String message)
    {
        state = ResultState.ERROR;
        errorMessage = message;

        if (errorMessage.length() > MAX_MESSAGE_LENGTH)
        {
            errorMessage = errorMessage.substring(0, MAX_MESSAGE_LENGTH);
        }
    }

    public void terminate(boolean timeout)
    {
        state = ResultState.TERMINATING;
        if (timeout)
        {
            errorMessage = "Timed out";
        }
        else
        {
            errorMessage = "Forcefully terminated";
        }
    }

    public void error(BuildException e)
    {
        error(e.getMessage());

        if (outputDir != null)
        {

            FileOutputStream fos = null;
            try
            {
                fos = new FileOutputStream(new File(outputDir, EXCEPTION_FILE));
                e.printStackTrace(new PrintStream(fos, true));
            }
            catch (FileNotFoundException ignored)
            {
                // no need to handle this, we did our level best
            }
            finally
            {
                IOUtils.close(fos);
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

    public String getFailureMessage()
    {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage)
    {
        this.failureMessage = failureMessage;
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

    public String getOutputDir()
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

    public void setOutputDir(String dir)
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

    public List<String> collectErrors()
    {
        List<String> errors = new LinkedList<String>();

        if (errorMessage != null)
        {
            errors.add(errorMessage);
        }

        if (failureMessage != null)
        {
            errors.add(failureMessage);
        }

        return errors;
    }

    public boolean hasMessages(Feature.Level level)
    {
        if (level == Feature.Level.ERROR)
        {
            if (errorMessage != null)
            {
                return true;
            }

            if (failureMessage != null)
            {
                return true;
            }
        }

        return false;
    }

    public Feature.Level getLevel(String name)
    {
        return Feature.Level.valueOf(name);
    }

}
