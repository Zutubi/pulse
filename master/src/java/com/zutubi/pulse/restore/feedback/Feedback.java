package com.zutubi.pulse.restore.feedback;

/**
 *
 *
 */
public class Feedback
{
    private static final int UNDEFINED = -1;

    private long startTime = UNDEFINED;
    private long finishTime = UNDEFINED;

    private int percentageComplete = UNDEFINED;

    private TaskStatus status = TaskStatus.PENDING;

    private String statusMessage;

    public void start()
    {
        startTime = System.currentTimeMillis();
        status = TaskStatus.IN_PROGRESS;
    }

    public boolean isStarted()
    {
        return status == TaskStatus.IN_PROGRESS;
    }

    public long getStartTime()
    {
        return startTime;
    }

    private void finish()
    {
        finishTime = System.currentTimeMillis();
        if (startTime == UNDEFINED)
        {
            startTime = finishTime;
        }
    }

    public void completed()
    {
        finish();
        status = TaskStatus.SUCCESS;
    }

    public void failed()
    {
        finish();
        status = TaskStatus.FAILURE;
    }

    public void errored()
    {
        finish();
        status = TaskStatus.ERROR;
    }

    public void aborted()
    {
        finish();
        status = TaskStatus.ABORTED;
    }

    public boolean isFinished()
    {
        return status != TaskStatus.PENDING && status != TaskStatus.IN_PROGRESS;
    }

    public long getFinishTime()
    {
        return finishTime;
    }

    public long getElapsedTime()
    {
        if (!isStarted())
        {
            return UNDEFINED;
        }

        if (!isFinished())
        {
            long currentTime = System.currentTimeMillis();
            return currentTime - startTime;
        }

        return finishTime - startTime;
    }

    public void setPercetageComplete(int percentage)
    {
        if (percentage > 100)
        {
            throw new IllegalArgumentException();
        }
        if (percentage < 0)
        {
            throw new IllegalArgumentException();
        }
        if (percentage < this.percentageComplete)
        {
            // going backwards??
        }
        this.percentageComplete = percentage;
    }

    public int getPercentageComplete()
    {
        if (isFinished())
        {
            return 100;
        }
        return percentageComplete;
    }

    public long getEstimatedTime()
    {
        if (percentageComplete == UNDEFINED)
        {
            return UNDEFINED;
        }
        if (percentageComplete == 100)
        {
            return 0;
        }

        long elapsedTime = getElapsedTime();
        if (elapsedTime <= 0)
        {
            return UNDEFINED;
        }

        int percentageRemaining = (100 - percentageComplete);
        return (elapsedTime / percentageComplete) * percentageRemaining;
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage)
    {
        this.statusMessage = statusMessage;
    }
}
