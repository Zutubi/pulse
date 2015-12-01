package com.zutubi.pulse.master.rest.model.setup;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.pulse.master.util.monitor.Task;

import java.util.List;

/**
 * Details of an in-progress setup operation (e.g. upgrade, restore).
 */
public class ProgressModel
{
    private String status;
    private List<TaskModel> tasks;
    private int percentComplete;
    private long elapsedMillis;

    public <T extends Task> ProgressModel(final Monitor<T> monitor)
    {
        if (monitor.isFinished())
        {
            if (monitor.isSuccessful())
            {
                status = "success";
            }
            else
            {
                status = "failed";
            }
        }
        else if (monitor.isStarted())
        {
            status = "running";
        }
        else
        {
            status = "pending";
        }

        tasks = Lists.newArrayList(Lists.transform(monitor.getTasks(), new Function<T, TaskModel>()
        {
            @Override
            public TaskModel apply(T input)
            {
                return new TaskModel(input, monitor.getProgress(input));
            }
        }));

        percentComplete = monitor.getPercentageComplete();
        elapsedMillis = monitor.getElapsedTime();
    }

    public String getStatus()
    {
        return status;
    }

    public List<TaskModel> getTasks()
    {
        return tasks;
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
