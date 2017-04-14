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

import java.util.HashMap;
import java.util.Map;

/**
 * The TaskExecutionContext stored context information relating to a tasks execution
 * environment.
 * 
 * It contains a reference to the Trigger that triggered the tasks execution.
 */
public class TaskExecutionContext
{
    private Map<String, Object> context = new HashMap<String, Object>();

    private Trigger trigger;

    public Object get(String key)
    {
        return context.get(key);
    }

    public void put(String key, Object value)
    {
        context.put(key, value);
    }

    /**
     * Get the trigger that caused this task execution.
     *
     * @return a trigger.
     */
    public Trigger getTrigger()
    {
        return trigger;
    }

    public void setTrigger(Trigger trigger)
    {
        this.trigger = trigger;
    }
}
