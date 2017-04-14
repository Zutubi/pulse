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
