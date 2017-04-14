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

package com.zutubi.pulse.master.scheduling;

/**
 * <class-comment/>
 */
public interface Task
{
    /**
     * The execute method should be implemented to handle the logic for handling a particular
     * task. It is the execute method that is called when a trigger is triggered.
     *
     * @param context is a task execution context configured with the context of this execution
     * such as the trigger that triggered resulting in the execution of this action.
     */
    public void execute(TaskExecutionContext context);

}
