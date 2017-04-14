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

import com.zutubi.util.time.Clock;
import com.zutubi.util.time.SystemClock;
import com.zutubi.util.time.TimeStamps;

public class TaskFeedback<T extends Task>
{
    static final int UNDEFINED = -1;

    private long startTime = UNDEFINED;

    private long finishTime = UNDEFINED;

    private int percentageComplete = UNDEFINED;

    private JobMonitor monitor;

    private T task;

    private TaskStatus status = TaskStatus.PENDING;
    
    private String statusMessage;

    private Clock clock = new SystemClock();

    public TaskFeedback(JobMonitor monitor, T task)
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
        startTime = clock.getCurrentTimeMillis();
    }

    private void finish()
    {
        finishTime = clock.getCurrentTimeMillis();
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
        if (startTime == UNDEFINED)
        {
            return UNDEFINED;
        }

        if (finishTime == UNDEFINED)
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

    public void setClock(Clock clock)
    {
        this.clock = clock;
    }
}
