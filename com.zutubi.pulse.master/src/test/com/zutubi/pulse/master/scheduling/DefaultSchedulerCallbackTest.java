package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.master.model.persistence.TriggerDao;
import com.zutubi.util.Constants;
import com.zutubi.util.NullaryProcedure;
import com.zutubi.util.bean.WiringObjectFactory;
import com.zutubi.util.junit.ZutubiTestCase;
import static org.mockito.Mockito.*;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

public class DefaultSchedulerCallbackTest extends ZutubiTestCase
{
    private DefaultScheduler scheduler;
    private WiringObjectFactory objectFactory;
    private org.quartz.Scheduler quartzScheduler;
    private TriggerDao triggerDao;

    public void setUp() throws Exception
    {
        super.setUp();

        objectFactory = new WiringObjectFactory();

        triggerDao = mock(TriggerDao.class);

        DefaultTriggerHandler triggerHandler = new DefaultTriggerHandler();
        triggerHandler.setTriggerDao(triggerDao);
        triggerHandler.setObjectFactory(objectFactory);

        SchedulerFactory schedFact = new StdSchedulerFactory();
        quartzScheduler = schedFact.getScheduler();
        quartzScheduler.setJobFactory(new QuartzTaskJobFactory(triggerHandler));
        quartzScheduler.start();

        SimpleSchedulerStrategy strategy = new SimpleSchedulerStrategy();
        strategy.setQuartzScheduler(quartzScheduler);

        scheduler = new DefaultScheduler();
        scheduler.setTriggerHandler(triggerHandler);
        scheduler.setTriggerDao(triggerDao);
        scheduler.register(strategy);
        objectFactory.initProperties(this);

    }

    @Override
    protected void tearDown() throws Exception
    {
        quartzScheduler.shutdown(true);
        scheduler.stop(true);

        super.tearDown();
    }

    public void testCallbacksAreNotPersistent() throws InterruptedException, SchedulingException
    {
        scheduler.start();

        CallbackCounter callback = new CallbackCounter();
        scheduler.registerCallback(callback, 100);

        verify(triggerDao, never()).save((Trigger) anyObject());
    }

    public void testCallbacksBeforeStartAreNotPersistent() throws SchedulingException
    {
        CallbackCounter callback = new CallbackCounter();
        scheduler.registerCallback(callback, 100);

        verify(triggerDao, never()).save((Trigger) anyObject());
    }

    public void testCallbackInterval() throws SchedulingException, InterruptedException
    {
        scheduler.start();

        CallbackCounter callback = new CallbackCounter();
        scheduler.registerCallback(callback, 100);
        Thread.sleep(500 * Constants.MILLISECOND);
        assertTrue(scheduler.unregisterCallback(callback));
        assertEquals(5, callback.getCount());

        // verify that unregister does infact stop the callbacks.
        Thread.sleep(300 * Constants.MILLISECOND);
        assertEquals(5, callback.getCount());
    }

    public void testCallbackDate() throws InterruptedException, SchedulingException
    {
        scheduler.start();

        Date time = new Date(System.currentTimeMillis() + (200 * Constants.MILLISECOND));
        CallbackCounter callback = new CallbackCounter();
        scheduler.registerCallback(callback, time);
        Thread.sleep(400 * Constants.MILLISECOND);
        assertEquals(1, callback.getCount());

        Thread.sleep(400 * Constants.MILLISECOND);
        assertEquals(1, callback.getCount());
    }

    public void testRegisterCallbackBeforeSchedulerStart() throws SchedulingException, InterruptedException
    {
        CallbackCounter callback = new CallbackCounter();
        scheduler.registerCallback(callback, 100);
        Thread.sleep(300 * Constants.MILLISECOND);
        assertEquals(0, callback.getCount());

        scheduler.start();
        Thread.sleep(300 * Constants.MILLISECOND);
        assertTrue(callback.getCount() >= 3);
    }

    public void testRegisterAndUnregisterCallbackBeforeSchedulerStart() throws InterruptedException, SchedulingException
    {
        CallbackCounter callback = new CallbackCounter();
        scheduler.registerCallback(callback, 100);
        Thread.sleep(300 * Constants.MILLISECOND);
        assertEquals(0, callback.getCount());
        assertTrue(scheduler.unregisterCallback(callback));

        scheduler.start();
        Thread.sleep(300 * Constants.MILLISECOND);
        assertEquals(0, callback.getCount());
    }

    private static class CallbackCounter implements NullaryProcedure
    {
        private int count = 0;
        public void process()
        {
            count++;
        }

        public int getCount()
        {
            return count;
        }
    }
}
