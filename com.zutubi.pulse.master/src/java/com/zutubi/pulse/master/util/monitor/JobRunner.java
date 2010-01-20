package com.zutubi.pulse.master.util.monitor;

import com.zutubi.util.logging.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class JobRunner
{
    private static final Logger LOG = Logger.getLogger(JobRunner.class);

    private Monitor monitor = null;

    public void run(List<Task> tasks)
    {
        run(new ListJobWrapper(tasks));
    }

    public void run(Task task)
    {
        run(new ArrayJobWrapper(task));
    }

    public void run(Task... tasks)
    {
        run(new ArrayJobWrapper(tasks));
    }

    public void run(Job job)
    {
        Monitor monitor = getMonitor();

        // register the tasks with the monitor.
        Iterator<Task> i = job.getTasks();
        while (i.hasNext())
        {
            monitor.add(i.next());
        }

        monitor.markStarted();

        boolean abort = false;
        try
        {
            List<Task> tasks = new LinkedList<Task>(monitor.getTasks());

            for (Task currentTask : tasks)
            {
                TaskFeedback taskTracker = monitor.getProgress(currentTask);

                if (currentTask instanceof FeedbackAware)
                {
                    ((FeedbackAware) currentTask).setFeedback(taskTracker);
                }

                taskTracker.markStarted();

                try
                {
                    if (!abort)
                    {
                        try
                        {
                            currentTask.execute();
                        }
                        catch (TaskException e)
                        {
                            throw e;
                        }
                        catch (Throwable t)
                        {
                            throw new TaskException(t);
                        }

                        if (currentTask.hasFailed())
                        {
                            // use an exception to break out to the task failure handling.
                            StringBuffer errors = new StringBuffer();
                            String sep = "\n";
                            for (String error : currentTask.getErrors())
                            {
                                errors.append(sep);
                                errors.append(error);
                            }

                            String message = "Task '" + currentTask.getName() + "' is marked as failed. The " +
                                    "following errors were recorded:" + errors.toString();

                            throw new TaskException(message);
                        }
                        else
                        {
                            taskTracker.markSuccessful();
                        }
                    }
                    else
                    {
                        taskTracker.markAborted();
                    }
                }
                catch (TaskException e)
                {
                    LOG.warning(e);
                    taskTracker.markFailed();
                    taskTracker.setStatusMessage("Failed. Cause: " + e.getMessage());

                    if (currentTask.haltOnFailure())
                    {
                        abort = true;
                    }
                }
            }
        }
        finally
        {
            if (abort)
            {
                monitor.markFailed();
            }
            else
            {
                monitor.markCompleted();
            }
        }
    }

    public Monitor getMonitor()
    {
        if (monitor == null)
        {
            synchronized (this)
            {
                if (monitor == null)
                {
                    monitor = new Monitor();
                }
            }
        }
        return monitor;
    }
}
