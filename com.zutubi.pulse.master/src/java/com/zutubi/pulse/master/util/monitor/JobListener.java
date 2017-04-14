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

/**
 * The job listener interface defines a set of callbacks that can be
 * implemented to track the execution of the tasks within a job.
 */
public interface JobListener<T extends Task>
{
    /**
     * Callback triggered when the execution of a task is started.
     *
     * @param task      the task being started
     * @param feedback  the feedback associated with the task
     */
    void taskStarted(T task, TaskFeedback<T> feedback);

    /**
     * Callback triggerd when the execution of a task fails.
     *
     * @param task      the task that failed
     * @param feedback  the feedback associated with the task
     */
    void taskFailed(T task, TaskFeedback<T> feedback);

    /**
     * Callback triggered when the execution of a task completes.
     *
     * @param task      the task that was completed
     * @param feedback  the feedback associated with the task
     */
    void taskCompleted(T task, TaskFeedback<T> feedback);

    /**
     * Callback triggered when the execution of a task is aborted.
     *
     * @param task      the task that was aborted
     * @param feedback  the feedback associated with the task
     */
    void taskAborted(T task, TaskFeedback feedback);
}
