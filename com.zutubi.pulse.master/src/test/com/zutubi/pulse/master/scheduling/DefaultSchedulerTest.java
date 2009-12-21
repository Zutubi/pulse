package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.master.model.persistence.InMemoryTriggerDao;
import com.zutubi.pulse.master.model.persistence.TriggerDao;
import com.zutubi.util.junit.ZutubiTestCase;

public class DefaultSchedulerTest extends ZutubiTestCase
{
    private DefaultScheduler scheduler;
    private TriggerDao triggerDao;

    public void setUp() throws Exception
    {
        super.setUp();

        // add setup code here.
        scheduler = new DefaultScheduler();
        TestTriggerHandler triggerHandler = new TestTriggerHandler();
        scheduler.setTriggerHandler(triggerHandler);
        triggerDao = new InMemoryTriggerDao();
        scheduler.setTriggerDao(triggerDao);

        scheduler.register(new NoopSchedulerStrategy());
        scheduler.start();
    }

    public void testPersistence() throws SchedulingException
    {
        Trigger trigger = new NoopTrigger("a", "a");
        trigger.setTaskClass(NoopTask.class);
        scheduler.schedule(trigger);

        Trigger persistentTrigger = triggerDao.findByNameAndGroup("a", "a");
        assertNotNull(persistentTrigger);
        assertEquals("a", persistentTrigger.getName());
        assertEquals("a", persistentTrigger.getGroup());
        assertEquals(NoopTask.class, persistentTrigger.getTaskClass());
    }

    public void testScheduledState() throws SchedulingException
    {
        Trigger trigger = new NoopTrigger("a", "a");
        trigger.setTaskClass(NoopTask.class);
        scheduler.schedule(trigger);

        Trigger scheduledTrigger = scheduler.getTrigger("a", "a");
        assertTrue(scheduledTrigger.isScheduled());
    }

    public void testUniqunessByNameAndGroup() throws SchedulingException
    {
        scheduler.schedule(new NoopTrigger("a", "a"));
        try
        {
            scheduler.schedule(new NoopTrigger("a", "a"));
            assertTrue(false);
        }
        catch (SchedulingException e)
        {
            // okay then :)
            assertEquals(e.getMessage(), "A trigger with name a and group a has already been scheduled.");
        }
    }

    public void testPauseGroup() throws SchedulingException
    {
        scheduler.schedule(new NoopTrigger("a"));
        assertEquals(TriggerState.SCHEDULED, scheduler.getTrigger("a", Trigger.DEFAULT_GROUP).getState());

        scheduler.pause(Trigger.DEFAULT_GROUP);
        assertEquals(TriggerState.PAUSED, scheduler.getTrigger("a", Trigger.DEFAULT_GROUP).getState());

        scheduler.resume(Trigger.DEFAULT_GROUP);
        assertEquals(TriggerState.SCHEDULED, scheduler.getTrigger("a", Trigger.DEFAULT_GROUP).getState());
    }

    /**
     * CIB-1264: renaming paused trigger re-enables it.
     *
     * @throws SchedulingException if an error occurs.
     */
    public void testUpdatePausedTriggerRemainsPaused() throws SchedulingException
    {
        NoopTrigger trigger = new NoopTrigger("a");

        scheduler.schedule(trigger);
        scheduler.pause(trigger);
        assertEquals(TriggerState.PAUSED, trigger.getState());

        trigger.setName("b");
        scheduler.update(trigger);
        assertEquals(TriggerState.PAUSED, trigger.getState());
    }
}