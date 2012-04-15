package com.zutubi.pulse.master.util.monitor;

import com.zutubi.util.time.Clock;
import com.zutubi.util.time.SystemClock;
import com.zutubi.util.time.TimeStamps;

import java.util.*;

public class JobMonitor<T extends Task> implements Monitor<T>
{
    private static final long UNDEFINED = -1;

    private long startTime = UNDEFINED;
    private long finishTime = UNDEFINED;

    private Clock clock = new SystemClock();

    private final Map<Object, TaskFeedback<T>> feedbacks = new HashMap<Object, TaskFeedback<T>>();

    private List<T> tasks = new LinkedList<T>();

    private T currentTask = null;

    private int completedTasks = 0;

    private boolean successful = true;

    private final List<JobListener<T>> listeners = new LinkedList<JobListener<T>>();

    public void add(JobListener<T> listener)
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

    TaskFeedback<T> add(T task)
    {
        TaskFeedback<T> feedback = feedbacks.get(task);
        if (feedback == null)
        {
            synchronized(feedbacks)
            {
                feedback = feedbacks.get(task);
                if (feedback == null)
                {
                    feedback = new TaskFeedback<T>(this, task);
                    feedbacks.put(task, feedback);
                    tasks.add(task);
                }
            }
        }
        return feedback;
    }

    public TaskFeedback<T> getProgress(T task)
    {
        return feedbacks.get(task);
    }

    void start(T task)
    {
        if (currentTask != null)
        {
            throw new IllegalStateException();
        }
        currentTask = task;

        for (JobListener<T> listener : listeners)
        {
            listener.taskStarted(task, getProgress(currentTask));
        }
    }

    void finish(T task)
    {
        currentTask = null;
        completedTasks++;

        for (JobListener<T> listener : listeners)
        {
            TaskFeedback<T> feedback = getProgress(task);
            if (feedback.isAborted())
            {
                listener.taskAborted(task, feedback);
            }
            else if (feedback.isFailed())
            {
                listener.taskFailed(task, feedback);
            }
            else
            {
                listener.taskCompleted(task, feedback);
            }
        }
    }

    void markFailed()
    {
        successful = false;
        if (finishTime == UNDEFINED)
        {
            finishTime = clock.getCurrentTimeMillis();
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

    void markStarted()
    {
        if (startTime == UNDEFINED)
        {
            startTime = clock.getCurrentTimeMillis();
        }
    }

    public boolean isStarted()
    {
        return startTime != UNDEFINED;
    }

    void markCompleted()
    {
        if (finishTime == UNDEFINED)
        {
            finishTime = clock.getCurrentTimeMillis();
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
            TaskFeedback<T> currentProgress = getCurrentTaskProgress();
            int currentTaskCompletion = (int)(singleTaskPercentage * (((double)currentProgress.getPercentageComplete()) / 100));

            if (currentTaskCompletion > 0)
            {
                completedPercentage = completedPercentage + currentTaskCompletion;
            }
        }

        return completedPercentage;
    }

    public T getCurrentTask()
    {
        return currentTask;
    }

    public TaskFeedback<T> getCurrentTaskProgress()
    {
        return getProgress(currentTask);
    }

    public List<T> getTasks()
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
            long currentTime = clock.getCurrentTimeMillis();
            return currentTime - startTime;
        }

        return finishTime - startTime;
    }

    public String getElapsedTimePretty()
    {
        return TimeStamps.getPrettyElapsed(getElapsedTime());
    }
}
