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

import com.zutubi.events.DefaultEventManager;
import com.zutubi.events.Event;
import com.zutubi.events.EventManager;
import com.zutubi.util.bean.DefaultObjectFactory;

public class EventSchedulerStrategyTest extends SchedulerStrategyTestBase
{
    private EventManager eventManager;
    private TestTriggerHandler triggerHandler;

    public void setUp() throws Exception
    {
        super.setUp();

        triggerHandler = new TestTriggerHandler();

        // add setup code here.
        scheduler = new EventSchedulerStrategy();
        eventManager = new DefaultEventManager();
        ((EventSchedulerStrategy)scheduler).setEventManager(eventManager);
        ((EventSchedulerStrategy)scheduler).setObjectFactory(new DefaultObjectFactory());
        scheduler.setTriggerHandler(triggerHandler);
    }

    public void testTriggerOnSpecificEvent() throws SchedulingException
    {
        EventTrigger trigger = new EventTrigger(TestEvent.class);
        scheduler.schedule(trigger);

        assertFalse(triggerHandler.wasTriggered());
        eventManager.publish(new Event(this));
        assertFalse(triggerHandler.wasTriggered());
        assertEquals(0, trigger.getTriggerCount());
        eventManager.publish(new TestEvent(this));
        assertTrue(triggerHandler.wasTriggered());
        assertEquals(1, trigger.getTriggerCount());
    }

    public void testFiltersEvent() throws SchedulingException
    {
        EventTrigger trigger = new EventTrigger(TestEvent.class);
        trigger.setFilterClass(BlockFilter.class);
        scheduler.schedule(trigger);
        eventManager.publish(new TestEvent(this));
        assertFalse(triggerHandler.wasTriggered());
        assertEquals(0, trigger.getTriggerCount());
        trigger.setFilterClass(PassFilter.class);
        eventManager.publish(new TestEvent(this));
        assertTrue(triggerHandler.wasTriggered());
        assertEquals(1, trigger.getTriggerCount());
    }

    @Override
    public void testTaskExecutedOnTrigger() throws SchedulingException
    {
        super.testTaskExecutedOnTrigger();
    }

    @Override
    public void testPauseTrigger() throws SchedulingException
    {
        super.testPauseTrigger();
    }

    @Override
    public void testTriggerCount() throws SchedulingException
    {
        super.testTriggerCount();
    }

    @Override
    protected TestTriggerHandler getHandler()
    {
        return triggerHandler;
    }

    protected void activateTrigger(Trigger trigger)
    {
        eventManager.publish(new Event(this));
    }

    protected Trigger createTrigger()
    {
        Trigger trigger = new EventTrigger();
        trigger.setTaskClass(NoopTask.class);
        return trigger;
    }

    private class TestEvent extends Event
    {
        public TestEvent(Object source)
        {
            super(source);
        }
    }

}