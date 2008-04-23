package com.zutubi.pulse.restore.feedback;

import com.zutubi.pulse.util.TimeStamps;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public class TaskMonitor
{
    private Map<Object, Feedback> feedbacks = new HashMap<Object, Feedback>();
    private List<Object> tasks = new LinkedList<Object>();

    private Object currentTask = null;

    private int completedTasks = 0;

    private boolean started = false;
    private boolean finished = false;

    private static final long UNDEFINED = -1;
    private long startTime = UNDEFINED;
    private long finishTime = UNDEFINED;

    private boolean successful = true;

    public Feedback add(Object task)
    {
        Feedback feedback = feedbacks.get(task);
        if (feedback == null)
        {
            feedback = new Feedback();
            feedbacks.put(task, feedback);
            tasks.add(task);
        }
        return feedback;
    }

    public Feedback getProgress(Object task)
    {
        return feedbacks.get(task);
    }

    public void start()
    {
        internalStart();
    }

    public Feedback started(Object task)
    {
        internalStart();
        
        if (currentTask != null)
        {
            throw new IllegalStateException();
        }
        currentTask = task;

        Feedback feedback = getProgress(task);
        feedback.start();
        
        return feedback;
    }

    private void internalStart()
    {
        started = true;
        if (startTime == UNDEFINED)
        {
            startTime = System.currentTimeMillis();
        }
    }

    public void failed()
    {
        Feedback feedback = getCurrentTaskProgress();
        feedback.failed();

        successful = false;

        finishCurrentTask();
    }

    public void completed()
    {
        Feedback feedback = getCurrentTaskProgress();
        feedback.completed();

        finishCurrentTask();
    }

    public void aborted()
    {
        Feedback feedback = getCurrentTaskProgress();
        feedback.aborted();

        successful = false;

        finishCurrentTask();
    }

    private void finishCurrentTask()
    {
        currentTask = null;
        completedTasks++;
    }

    public void finish()
    {
        if (!finished)
        {
            finished = true;
            finishTime = System.currentTimeMillis();
        }
    }

    public Object getCurrentTask()
    {
        return currentTask;
    }

    public Feedback getCurrentTaskProgress()
    {
        return getProgress(currentTask);
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
            Feedback currentProgress = getCurrentTaskProgress();
            int currentTaskCompletion = (int)(singleTaskPercentage * (((double)currentProgress.getPercentageComplete()) / 100));

            if (currentTaskCompletion > 0)
            {
                completedPercentage = completedPercentage + currentTaskCompletion;
            }
        }

        return completedPercentage;
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

    public boolean isStarted()
    {
        return started;
    }

    public boolean isFinished()
    {
        return finished;
    }

    public boolean isSuccessful()
    {
        return successful;
    }

    public List<Object> getTasks()
    {
        return tasks;
    }
}
