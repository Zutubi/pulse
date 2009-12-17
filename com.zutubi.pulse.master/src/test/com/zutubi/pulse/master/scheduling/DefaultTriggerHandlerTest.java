package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.model.persistence.InMemoryTriggerDao;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <class-comment/>
 */
public class DefaultTriggerHandlerTest extends PulseTestCase
{
    private DefaultTriggerHandler handler;
    private InMemoryTriggerDao triggerDao;
    private ThreadPoolExecutor executor;

    /**
     * This lock is used to ensure correct synchronisation between the test case requesting the execution
     * of the trigger and the task being triggered.
     */
    private static final Object lock = new Object();
    /**
     * The waiting condition.
     */
    private static boolean waiting;

    protected void setUp() throws Exception
    {
        super.setUp();

        handler = new DefaultTriggerHandler();
        handler.setObjectFactory(new DefaultObjectFactory());
        triggerDao = new InMemoryTriggerDao();
        handler.setTriggerDao(triggerDao);

        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
    }

    protected void tearDown() throws Exception
    {
        executor.shutdown();
        handler = null;
        triggerDao = null;

        super.tearDown();
    }

    public void testTriggerDoesNotFireConcurrently() throws SchedulingException, InterruptedException
    {
        Trigger a = new NoopTrigger("a", "a");
        a.setTaskClass(BlockingTask.class);
        triggerDao.save(a);

        // assert that the initial trigger count is 0.
        assertEquals(0, a.getTriggerCount());

        // fire the trigger in parallel, and wait for the triggered task to indicate that it has started.
        fireAndWaitForCallback(a);

        // the fired trigger is now 'executing' until we tell it to stop. Therefore, we expect the trigger count to
        // remain at 1, regardless of how many times we fire the trigger now, since concurrent triggers are ignored.
        assertEquals(1, a.getTriggerCount());
        fireInline(a);
        assertEquals(1, a.getTriggerCount());
        fireInline(a);
        assertEquals(1, a.getTriggerCount());

        // tell the executing task to stop and wait for it to stop.
        // - trigger the task to stop and then wait for it do finish.
        BlockingTask.stopWaiting();
        while (executor.getActiveCount() > 0)
        {
            Thread.yield();
        }
        // wait for thread to stop

        assertEquals(1, a.getTriggerCount());

        // now fire the trigger again and ensure that the trigger count is now increased as expected.
        fireAndWaitForCallback(a);

        assertEquals(2, a.getTriggerCount());

        BlockingTask.stopWaiting();
    }

    public static void stopWaiting()
    {
        synchronized(lock)
        {
            if (waiting)
            {
                waiting = false;
                lock.notifyAll();
            }
        }
    }

    private void fireInline(final Trigger trigger) throws SchedulingException
    {
        handler.fire(trigger);
    }

    private void fireAndWaitForCallback(Trigger a)
    {
        synchronized(lock)
        {
            waiting = true;
            fireInParallel(a);
            while (waiting)
            {
                try
                {
                    lock.wait();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

    private void fireInParallel(final Trigger trigger)
    {
        // we want this thread to execute immediately.
        executor.execute(new Runnable()
        {
            public void run()
            {
                try
                {
                    handler.fire(trigger);
                }
                catch (SchedulingException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

}


