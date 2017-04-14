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

import java.util.List;

/**
 * The task interface represents the smallest unit of work that can be monitored
 * by the monitoring system.
 */
public interface Task
{
    /**
     * Get a human readable name of this upgrade task. This name must uniquely identify this
     * task.
     *
     * @return the task name.
     */
    String getName();

    /**
     * Get a human readable description of what this task does. This will be
     * displayed to the user to inform them of what is happening.
     *
     * @return a short descriptive message
     */
    String getDescription();

    /**
     * The list of errors that this task encountered during execution.
     *
     * @return a list of error messages
     */
    List<String> getErrors();

    /**
     * If this task is part of a group of tasks being executed, and it fails, the
     * halt on failure property will indicate whether or not the execution of the
     * remaining tasks should continue.
     *
     * For example: If this upgrade task that changes the structure of some data
     * (such as adding a column to the database) and it fails, then haltOnFailure
     * should return true since subsequent upgrade task may rely on the existance
     * of that column. 
     *
     * @return boolean indicating whether or not to halt processing on task failure.
     */
    boolean haltOnFailure();

    /**
     * Indicates whether or not the processing of this task has failed.
     *
     * @return true if the task has failed, false otherwise.
     */
    boolean hasFailed();

    /**
     * Run this upgrade task.
     *
     * @throws TaskException if a problem occured during task execution.
     */
    void execute() throws TaskException;
}
