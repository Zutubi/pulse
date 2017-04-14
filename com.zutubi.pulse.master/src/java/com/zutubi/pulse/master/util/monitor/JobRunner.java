/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.util.monitor;

import com.zutubi.util.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class JobRunner<T extends Task>
{
    private static final Logger LOG = Logger.getLogger(JobRunner.class);

    private JobMonitor<T> monitor = null;

    public void run(List<T> tasks)
    {
        run(new ListJobWrapper<>(tasks));
    }

    public void run(T task)
    {
        run(new ArrayJobWrapper<>(task));
    }

    public void run(T... tasks)
    {
        run(new ArrayJobWrapper<>(tasks));
    }

    public void run(Job<T> job)
    {
        JobMonitor<T> monitor = (JobMonitor<T>) getMonitor();

        // register the tasks with the monitor.
        for (T aJob : job)
        {
            monitor.add(aJob);
        }

        monitor.markStarted();

        boolean abort = false;
        try
        {
            List<T> tasks = new ArrayList<>(monitor.getTasks());

            for (T currentTask : tasks)
            {
                TaskFeedback<T> taskTracker = monitor.getProgress(currentTask);

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
                            StringBuilder errors = new StringBuilder();
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

    public Monitor<T> getMonitor()
    {
        if (monitor == null)
        {
            synchronized (this)
            {
                if (monitor == null)
                {
                    monitor = new JobMonitor<>();
                }
            }
        }
        return monitor;
    }
}
