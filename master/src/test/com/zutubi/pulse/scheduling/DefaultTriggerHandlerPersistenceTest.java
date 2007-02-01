package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.model.persistence.hibernate.PersistenceTestCase;
import com.zutubi.pulse.model.persistence.TriggerDao;

/**
 * <class-comment/>
 */
public class DefaultTriggerHandlerPersistenceTest extends PersistenceTestCase
{
    private TriggerHandler handler;
    private TriggerDao triggerDao;

    public DefaultTriggerHandlerPersistenceTest()
    {
    }

    public DefaultTriggerHandlerPersistenceTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
/*
        super.setUp();

        handler = (TriggerHandler) ComponentContext.getBean("triggerHandler");
        triggerDao = (TriggerDao) ComponentContext.getBean("triggerDao");
*/
    }

    protected void tearDown() throws Exception
    {
//        super.tearDown();
    }

    protected String[] getConfigLocations()
    {
        return new String[]{"com/zutubi/pulse/bootstrap/testBootstrapContext.xml",
                "com/zutubi/pulse/scheduling/DefaultTriggerHandlerPersistenceTestContext.xml",
                "com/zutubi/pulse/bootstrap/context/schedulingContext.xml"};
    }

    public void test()
    {
        // empty
    }
    /**
     * We need to ensure that when a trigger fires, its state changes
     * are persisted regardless of the outcome of the task execution.
     */
/*
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
*/

/*
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
*/

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
