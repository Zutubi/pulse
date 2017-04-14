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
public class TestTriggerHandler implements TriggerHandler
{
    private long triggerCount;
    private boolean triggered;

    public void fire(Trigger trigger) throws SchedulingException
    {
        fire(trigger, new TaskExecutionContext());
    }

    public void fire(Trigger trigger, TaskExecutionContext context) throws SchedulingException
    {
        trigger.fire();
        triggerCount++;
        triggered = true;
    }

    public boolean wasTriggered()
    {
        return triggered;
    }

    public long getTriggerCount()
    {
        return triggerCount;
    }

    public void reset()
    {
        triggerCount = 0;
        triggered = false;
    }
}
