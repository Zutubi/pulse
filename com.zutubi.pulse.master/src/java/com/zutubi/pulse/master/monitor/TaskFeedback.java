package com.zutubi.pulse.master.monitor;

import com.zutubi.util.TimeStamps;

/**
 *
 *
 */
public class TaskFeedback
{
    private static final int UNDEFINED = -1;

    private long startTime = UNDEFINED;

    private long finishTime = UNDEFINED;

    private int percentageComplete = UNDEFINED;

    private Monitor monitor;

    private Task task;

    private TaskStatus status = TaskStatus.PENDING;
    
    private String statusMessage;

    public TaskFeedback(Monitor monitor, Task task)
    {
        this.monitor = monitor;
        this.task = task;
    }

    public void markStarted()
    {
        status = TaskStatus.IN_PROGRESS;
        start();
        monitor.start(task);
    }

    public boolean isStarted()
    {
        return status == TaskStatus.IN_PROGRESS;
    }

    public void markFailed()
    {
        status = TaskStatus.FAILED;
        finish();
        monitor.finish(task);
    }

    public boolean isFailed()
    {
        return status == TaskStatus.FAILED;
    }

    public void markAborted()
    {
        finish();
        status = TaskStatus.ABORTED;
        monitor.finish(task);
    }

    public boolean isAborted()
    {
        return status == TaskStatus.ABORTED;
    }

    public void markSuccessful()
    {
        finish();
        status = TaskStatus.SUCCESS;
        monitor.finish(task);
    }

    public boolean isSuccessful()
    {
        return status == TaskStatus.SUCCESS;
    }

    public boolean isFinished()
    {
        return isAborted() || isSuccessful() || isFailed();
    }

    private void start()
    {
        startTime = System.currentTimeMillis();
    }

    private void finish()
    {
        finishTime = System.currentTimeMillis();
        if (startTime == UNDEFINED)
        {
            startTime = finishTime;
        }
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

    public String getPercentageCompletePretty()
    {
        int percentage = getPercentageComplete();
        if (percentage == UNDEFINED)
        {
            return "unknown";
        }
        return Integer.toString(percentage);
    }

    public int getPercentageRemaining()
    {
        int complete = getPercentageComplete();
        if (complete == UNDEFINED)
        {
            return UNDEFINED;
        }
        return 100 - complete;
    }

    public String getPercentageRemainingPretty()
    {
        int percentage = getPercentageRemaining();
        if (percentage == UNDEFINED)
        {
            return "unknown";
        }
        return Integer.toString(percentage);
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

    public String getElapsedTimePretty()
    {
        return TimeStamps.getPrettyElapsed(getElapsedTime());
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

        if (percentageComplete == 0)
        {
            return UNDEFINED;
        }

        long elapsedTime = getElapsedTime();
        if (elapsedTime <= 0)
        {
            return UNDEFINED;
        }

        long percentageRemaining = (100 - percentageComplete);
        return (elapsedTime / percentageComplete) * percentageRemaining;
    }

    public String getEstimatedTimePretty()
    {
        long l = getEstimatedTime();
        if (l == UNDEFINED)
        {
            return "unknown";
        }
        return TimeStamps.getPrettyEstimated(l);
    }

    public String getStatusMessage()
    {
        return (statusMessage != null) ? statusMessage : "";
    }

    public void setStatusMessage(String statusMessage)
    {
        this.statusMessage = statusMessage;
    }

}
