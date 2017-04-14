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

import com.zutubi.events.Event;

/**
 * Allows event triggers to filter incoming events based on data in the
 * trigger and/or event.
 */
public interface EventTriggerFilter
{
    /**
     * Filters acceptable events based on the trigger and event instances.
     *
     * @param trigger the trigger that is checking the event
     * @param event   the event to check
     * @param context the context in which the trigger task will execute if
     *                the event is accepted - filters may populate the
     *                context to pass information to the task
     * @return true iff this filter accepts this event
     */
    boolean accept(Trigger trigger, Event event, TaskExecutionContext context);
}
