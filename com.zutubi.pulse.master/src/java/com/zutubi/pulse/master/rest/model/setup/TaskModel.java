package com.zutubi.pulse.master.rest.model.setup;

import com.zutubi.pulse.master.util.monitor.Task;
import com.zutubi.pulse.master.util.monitor.TaskFeedback;

/**
 * Models a monitored task.
 */
public class TaskModel
{
    private String name;
    private String description;
    private String status;
    private String statusMessage;
    private int percentComplete;
    private long elapsedMillis;

    public TaskModel(Task task, TaskFeedback<?> feedback)
    {
        name = task.getName();
        description = task.getDescription();
        if (feedback != null)
        {
            if (feedback.isFinished())
            {
                if (feedback.isSuccessful())
                {
                    status = "success";
                }
                else if (feedback.isFailed())
                {
                    status = "failed";
                }
                else
                {
                    status = "aborted";
                }
            }
            else if (feedback.isStarted())
            {
                status = "running";
            }
            else
            {
                status = "pending";
            }

            statusMessage = feedback.getStatusMessage();
            percentComplete = feedback.getPercentageComplete();
            elapsedMillis = feedback.getElapsedTime();
        }
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getStatus()
    {
        return status;
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }

    public int getPercentComplete()
    {
        return percentComplete;
    }

    public long getElapsedMillis()
    {
        return elapsedMillis;
    }
}
