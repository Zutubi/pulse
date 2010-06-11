package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.pulse.master.model.persistence.TriggerDao;
import com.zutubi.pulse.master.model.persistence.hibernate.PersistenceTestCase;

public class DefaultTriggerHandlerPersistenceTest extends PersistenceTestCase
{
    private TriggerHandler handler;
    private TriggerDao triggerDao;

    protected void setUp() throws Exception
    {
        super.setUp();

        handler = (TriggerHandler) SpringComponentContext.getBean("triggerHandler");
        triggerDao = (TriggerDao) SpringComponentContext.getBean("triggerDao");
    }

    protected String[] getConfigLocations()
    {
        return new String[]{"com/zutubi/pulse/master/bootstrap/testBootstrapContext.xml",
                "com/zutubi/pulse/master/bootstrap/context/hibernateContext.xml",
                "com/zutubi/pulse/master/bootstrap/context/schedulingContext.xml"};
    }

    /**
     * We need to ensure that when a trigger fires, its state changes
     * are persisted regardless of the outcome of the task execution.
     */
    public void testTriggerStatePersistedWhenTaskThrowsException()
    {
        // configure the trigger.
        NoopTrigger trigger = new NoopTrigger("testTriggerStatePersistedWhenTaskThrowsException");
        trigger.setTaskClass(GenerateExceptionTask.class);
        triggerDao.save(trigger);

        // assert its initial state.
        Trigger persistentTrigger = triggerDao.findById(trigger.getId());
        assertEquals(0, persistentTrigger.getTriggerCount());

        // fire the trigger, ensuring that the expected exception is generated.
        try
        {
            handler.fire(trigger);
            fail();
        }
        catch (SchedulingException e)
        {
        }

        // assert that its state change was persisted.
        persistentTrigger = triggerDao.findById(trigger.getId());
        assertEquals(1, persistentTrigger.getTriggerCount());
    }

    public void testTriggerStatePersistenceAcrossTransactions() throws SchedulingException
    {
        NoopTrigger trigger = new NoopTrigger("testTriggerStatePersistenceAcrossTransactions");
        trigger.setTaskClass(NoopTask.class);
        triggerDao.save(trigger);

        // assert its initial state.
        Trigger persistentTrigger = triggerDao.findById(trigger.getId());
        assertEquals(0, persistentTrigger.getTriggerCount());

        commitAndRefreshTransaction();
        handler.fire(trigger);
        commitAndRefreshTransaction();

        persistentTrigger = triggerDao.findById(trigger.getId());
        assertEquals(1, persistentTrigger.getTriggerCount());
    }

    /**
     * Task implementation that generates a RuntimeException.
     */
    private class GenerateExceptionTask implements Task
    {
        public void execute(TaskExecutionContext context)
        {
            throw new RuntimeException();
        }
    }
}
