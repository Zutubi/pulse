package com.zutubi.pulse.master.util.monitor;

import com.zutubi.util.TimeStamps;

import java.util.*;

/**
 * The monitor provides a way to track the progress of the execution of a set
 * of tasks.
 */
public class Monitor
{
    private static final long UNDEFINED = -1;
    private long startTime = UNDEFINED;
    private long finishTime = UNDEFINED;

    private final Map<Object, TaskFeedback> feedbacks = new HashMap<Object, TaskFeedback>();

    private List<Task> tasks = new LinkedList<Task>();

    private Task currentTask = null;

    private int completedTasks = 0;

    private boolean successful = true;

    private final List<JobListener> listeners = new LinkedList<JobListener>();

    public void add(JobListener listener)
    {
        if (!listeners.contains(listener))
        {
            synchronized(listeners)
            {
                if (!listeners.contains(listener))
                {
                    listeners.add(listener);
                }
            }
        }
    }

    public TaskFeedback add(Task task)
    {
        TaskFeedback feedback = feedbacks.get(task);
        if (feedback == null)
        {
            synchronized(feedbacks)
            {
                feedback = feedbacks.get(task);
                if (feedback == null)
                {
                    feedback = new TaskFeedback(this, task);
                    feedbacks.put(task, feedback);
                    tasks.add(task);
                }
            }
        }
        return feedback;
    }

    public TaskFeedback getProgress(Task task)
    {
        return feedbacks.get(task);
    }

    void start(Task task)
    {
        if (currentTask != null)
        {
            throw new IllegalStateException();
        }
        currentTask = task;

        for (JobListener listener : listeners)
        {
            listener.taskStarted(task);
        }
    }

    void finish(Task task)
    {
        currentTask = null;
        completedTasks++;

        for (JobListener listener : listeners)
        {
            TaskFeedback feedback = getProgress(task);
            if (feedback.isAborted())
            {
                listener.taskAborted(task);
            }
            else if (feedback.isFailed())
            {
                listener.taskFailed(task);
            }
            else
            {
                listener.taskCompleted(task);
            }
        }
    }

    public void markFailed()
    {
        successful = false;
        if (finishTime == UNDEFINED)
        {
            finishTime = System.currentTimeMillis();
        }
    }

    public boolean isFailed()
    {
        return isFinished() && !successful;
    }

    // backwards compatibility with UI layer.
    public boolean isError()
    {
        return isFailed();
    }

    public void markStarted()
    {
        if (startTime == UNDEFINED)
        {
            startTime = System.currentTimeMillis();
        }
    }

    public boolean isStarted()
    {
        return startTime != UNDEFINED;
    }

    public void markCompleted()
    {
        if (finishTime == UNDEFINED)
        {
            finishTime = System.currentTimeMillis();
        }
    }

    public boolean isSuccessful()
    {
        return isFinished() && !isFailed();
    }

    public boolean isFinished()
    {
        return finishTime != UNDEFINED;
    }

    public int getCompletedTasks()
    {
        return completedTasks;
    }
    
    public int getPercentageComplete()
    {
        int numberOfTasks = feedbacks.size();

        if (numberOfTasks == 0)
        {
            return 0;
        }

        int completedPercentage = (100 * completedTasks / numberOfTasks);

        // add the small amount contributed by the current running task.
        if (currentTask != null)
        {
            int singleTaskPercentage = (100 / numberOfTasks);
            TaskFeedback currentProgress = getCurrentTaskProgress();
            int currentTaskCompletion = (int)(singleTaskPercentage * (((double)currentProgress.getPercentageComplete()) / 100));

            if (currentTaskCompletion > 0)
            {
                completedPercentage = completedPercentage + currentTaskCompletion;
            }
        }

        return completedPercentage;
    }

    public Task getCurrentTask()
    {
        return currentTask;
    }

    public TaskFeedback getCurrentTaskProgress()
    {
        return getProgress(currentTask);
    }

    public List<Task> getTasks()
    {
        return Collections.unmodifiableList(tasks);
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
}
