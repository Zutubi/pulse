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

import com.zutubi.util.Constants;

public class LongRunningNoopTask extends AbstractTask implements FeedbackAware
{
    private long duration;

    private TaskFeedback feedback;

    public LongRunningNoopTask(long duration)
    {
        super("Long running task.");

        this.duration = duration;
    }

    public void execute() throws TaskException
    {
        long startTime = System.currentTimeMillis();

        long projectedEndTime = startTime + duration;

        long currentTime = startTime;
        while (currentTime < projectedEndTime )
        {
            feedback.setPercetageComplete( (int)((currentTime - startTime) * 100 / duration) );
            try
            {
                Thread.sleep(Constants.SECOND);
            }
            catch (InterruptedException e)
            {
                // noop - ignore interruptions.
            }
            currentTime = System.currentTimeMillis();
        }
        feedback.setPercetageComplete(100);
    }

    public void setFeedback(TaskFeedback feedback)
    {
        this.feedback = feedback;
    }
}
