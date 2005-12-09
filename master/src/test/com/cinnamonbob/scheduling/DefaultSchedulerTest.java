package com.cinnamonbob.scheduling;

import junit.framework.*;
import com.cinnamonbob.scheduling.persistence.mock.MockTriggerDao;
import com.cinnamonbob.scheduling.persistence.mock.MockTaskDao;
import com.cinnamonbob.scheduling.persistence.TaskDao;
import com.cinnamonbob.scheduling.persistence.TriggerDao;

/**
 * <class-comment/>
 */
public class DefaultSchedulerTest extends TestCase
{
    private DefaultScheduler scheduler;
    private TaskDao taskDao;
    private TriggerDao triggerDao;

    public DefaultSchedulerTest(String testName)
    {
        super(testName);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        scheduler = new DefaultScheduler();
        triggerDao = new MockTriggerDao();
        taskDao = new MockTaskDao();
        scheduler.setTriggerDao(triggerDao);
        scheduler.setTaskDao(taskDao);
        scheduler.register(new NoopSchedulerStrategy());
    }

    public void tearDown() throws Exception
    {
        // add tear down code here.

        super.tearDown();
    }

    public void testPersistence() throws SchedulingException
    {
        scheduler.schedule(new NoopTrigger("a", "a"), new NoopTask("a", "a"));

        Trigger persistentTrigger = triggerDao.findByNameAndGroup("a", "a");
        assertNotNull(persistentTrigger);
        assertEquals("a", persistentTrigger.getName());
        assertEquals("a", persistentTrigger.getGroup());
        assertEquals("a", persistentTrigger.getTaskName());
        assertEquals("a", persistentTrigger.getTaskGroup());

        Task persistentTask = taskDao.findByNameAndGroup("a", "a");
        assertNotNull(persistentTask);
        assertEquals("a", persistentTask.getName());
        assertEquals("a", persistentTask.getGroup());
    }

    public void testUniqunessByNameAndGroup() throws SchedulingException
    {
        scheduler.schedule(new NoopTrigger("a", "a"), new NoopTask("a", "a"));
        try
        {
            scheduler.schedule(new NoopTrigger("a", "a"), new NoopTask("a", "a"));
            assertTrue(false);
        }
        catch (SchedulingException e)
        {
            // okay then :)
            assertEquals(e.getMessage(), "A trigger with name a and group a has already been registered.");
        }
        try
        {
            scheduler.schedule(new NoopTrigger("b", "b"), new NoopTask("a", "a"));
            assertTrue(false);
        }
        catch (SchedulingException e)
        {
            // okay then :)
            assertEquals(e.getMessage(), "A task with name a and group a has already been registered.");
        }

    }

    public void testPauseGroup() throws SchedulingException
    {
        scheduler.schedule(new NoopTrigger("a"), new NoopTask("a"));
        scheduler.schedule(new NoopTrigger("b"), new NoopTask("b"));
        scheduler.schedule(new NoopTrigger("c"), new NoopTask("c"));

        assertEquals(TriggerState.ACTIVE, scheduler.getTrigger("a", Trigger.DEFAULT_GROUP).getState());
        assertEquals(TriggerState.ACTIVE, scheduler.getTrigger("b", Trigger.DEFAULT_GROUP).getState());
        assertEquals(TriggerState.ACTIVE, scheduler.getTrigger("c", Trigger.DEFAULT_GROUP).getState());

        scheduler.pause(Trigger.DEFAULT_GROUP);

        assertEquals(TriggerState.PAUSED, scheduler.getTrigger("a", Trigger.DEFAULT_GROUP).getState());
        assertEquals(TriggerState.PAUSED, scheduler.getTrigger("b", Trigger.DEFAULT_GROUP).getState());
        assertEquals(TriggerState.PAUSED, scheduler.getTrigger("c", Trigger.DEFAULT_GROUP).getState());

        scheduler.resume(Trigger.DEFAULT_GROUP);

        assertEquals(TriggerState.ACTIVE, scheduler.getTrigger("a", Trigger.DEFAULT_GROUP).getState());
        assertEquals(TriggerState.ACTIVE, scheduler.getTrigger("b", Trigger.DEFAULT_GROUP).getState());
        assertEquals(TriggerState.ACTIVE, scheduler.getTrigger("c", Trigger.DEFAULT_GROUP).getState());

    }
}