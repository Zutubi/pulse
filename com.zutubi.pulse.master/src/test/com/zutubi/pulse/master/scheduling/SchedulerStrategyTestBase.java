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

import com.zutubi.util.junit.ZutubiTestCase;

public abstract class SchedulerStrategyTestBase extends ZutubiTestCase
{
    protected SchedulerStrategy scheduler = null;

    public void testTaskExecutedOnTrigger() throws SchedulingException
    {
        Trigger trigger = createTrigger();
        scheduler.schedule(trigger);

        // test.
        assertFalse(getHandler().wasTriggered());
        activateTrigger(trigger);
        assertTrue(getHandler().wasTriggered());

        // unschedule
        getHandler().reset();
        scheduler.unschedule(trigger);

        // test
        assertFalse(getHandler().wasTriggered());
        activateTrigger(trigger);
        assertFalse(getHandler().wasTriggered());
    }

    public void testPauseTrigger() throws SchedulingException
    {
        Trigger trigger = createTrigger();
        scheduler.schedule(trigger);
        assertEquals(0, trigger.getTriggerCount());
        activateTrigger(trigger);
        assertEquals(1, trigger.getTriggerCount());
        scheduler.pause(trigger);
        activateTrigger(trigger);
        assertEquals(1, trigger.getTriggerCount());
        scheduler.resume(trigger);
        activateTrigger(trigger);
        assertEquals(2, trigger.getTriggerCount());
    }

    public void testTriggerCount() throws SchedulingException
    {
        // schedule
        Trigger trigger = createTrigger();
        scheduler.schedule(trigger);
        assertEquals(0, trigger.getTriggerCount());
        activateTrigger(trigger);
        assertEquals(1, trigger.getTriggerCount());
        activateTrigger(trigger);
        assertEquals(2, trigger.getTriggerCount());
        scheduler.unschedule(trigger);
        activateTrigger(trigger);
        assertEquals(2, trigger.getTriggerCount());
    }

    protected abstract Trigger createTrigger();
    protected abstract void activateTrigger(Trigger trigger) throws SchedulingException;
    protected abstract TestTriggerHandler getHandler();
}