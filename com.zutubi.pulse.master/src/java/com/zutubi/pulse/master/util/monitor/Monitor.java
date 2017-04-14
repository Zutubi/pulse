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

import java.util.*;

/**
 * The monitor provides a way to track the progress of the execution of a set
 * of tasks.
 */
public interface Monitor<T extends Task>
{
    /**
     * Add a job listener to the monitor.  The listener will then receive
     * the appropriate callbacks for the tasks being monitored.
     *
     * @param listener  the listener being registered.
     */
    void add(JobListener<T> listener);

    /**
     * Get the task feedback for the specific task.
     *
     * @param task  the task for which the feedback is requested.
     *
     * @return the feedback for the specified task.
     */
    TaskFeedback<T> getProgress(T task);

    /**
     * @return true if the monitored job has finished and was not successful.
     */
    boolean isFailed();

    /**
     * @return true if the monitored job has started running.
     */
    boolean isStarted();

    /**
     * @return true if the monitored job has finished running and is considered to have
     * done so successfully.  That is, no tasks that are not allowed to fail failed.
     */
    boolean isSuccessful();

    /**
     * @return true if the monitored job has finished, that is all of the tasks have been
     * completed.
     */
    boolean isFinished();

    /**
     * @return the number of tasks that have been completed at the time of asking.
     */
    int getCompletedTasks();

    /**
     * @return the percentage completed.  This is a number between 0 and 100.
     */
    int getPercentageComplete();

    /**
     * @return the task that is currently being run, or null if no task is being run.
     */
    T getCurrentTask();

    /**
     * @return the feedback for the task that is currently being run.
     */
    TaskFeedback<T> getCurrentTaskProgress();

    /**
     * @return a list of all the tasks associated with the job being monitored.
     */
    List<T> getTasks();

    /**
     * Return the elapsed time between the monitored job starting and it finishing.  If the
     * job has not finished, then the elapsed time is between the start and time of this request.
     *
     * @return the elapsed time in milliseconds.
     */
    long getElapsedTime();

    /**
     * @return a human readable version of the elapsed time.
     *
     * @see #getElapsedTime() 
     */
    String getElapsedTimePretty();
}
