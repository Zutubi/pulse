package com.zutubi.pulse.master.scheduling;

import com.zutubi.pulse.master.model.persistence.TriggerDao;
import com.zutubi.util.Constants;
import com.zutubi.util.NullaryProcedure;
import com.zutubi.util.bean.WiringObjectFactory;
import com.zutubi.util.junit.ZutubiTestCase;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

import static org.mockito.Mockito.*;

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

        CountCallback callback = new CountCallback();
        scheduler.registerCallback(callback, 100);

        verify(triggerDao, never()).save((Trigger) anyObject());
    }

    public void testCallbacksBeforeStartAreNotPersistent() throws SchedulingException
    {
        CountCallback callback = new CountCallback();
        scheduler.registerCallback(callback, 100);

        verify(triggerDao, never()).save((Trigger) anyObject());
    }

    public void testCallbackInterval() throws SchedulingException, InterruptedException
    {
        CountCallback callback = new CountCallback();
        scheduler.registerCallback(callback, 100);
        scheduler.start();
        Thread.sleep(500 * Constants.MILLISECOND);
        assertTrue(scheduler.unregisterCallback(callback));
        int countAfterUnregister = callback.getCount();
        assertTrue(countAfterUnregister >= 5);

        int count = callback.getCount();
        assertTrue(6 >= count && count >= 4);

        // verify that unregister does infact stop the callbacks.
        Thread.sleep(300 * Constants.MILLISECOND);
        assertEquals(countAfterUnregister, callback.getCount());
    }

    public void testCallbackDate() throws InterruptedException, SchedulingException
    {
        Date time = new Date(System.currentTimeMillis() + (200 * Constants.MILLISECOND));
        CountCallback callback = new CountCallback();
        scheduler.registerCallback(callback, time);
        scheduler.start();
        Thread.sleep(400 * Constants.MILLISECOND);
        assertEquals(1, callback.getCount());

        Thread.sleep(400 * Constants.MILLISECOND);
        assertEquals(1, callback.getCount());
    }

    public void testRegisterCallbackBeforeSchedulerStart() throws SchedulingException, InterruptedException
    {
        CountCallback callback = new CountCallback();
        scheduler.registerCallback(callback, 100);
        Thread.sleep(300 * Constants.MILLISECOND);
        assertEquals(0, callback.getCount());

        scheduler.start();
        Thread.sleep(300 * Constants.MILLISECOND);
        assertTrue(callback.getCount() >= 2);
    }

    public void testRegisterAndUnregisterCallbackBeforeSchedulerStart() throws InterruptedException, SchedulingException
    {
        CountCallback callback = new CountCallback();
        scheduler.registerCallback(callback, 100);
        Thread.sleep(300 * Constants.MILLISECOND);
        assertEquals(0, callback.getCount());
        assertTrue(scheduler.unregisterCallback(callback));

        scheduler.start();
        Thread.sleep(300 * Constants.MILLISECOND);
        assertEquals(0, callback.getCount());
    }

    public void testCallbackThrowingException() throws SchedulingException, InterruptedException
    {
        ExceptionCallback callback = new ExceptionCallback();
        scheduler.registerCallback(callback, 100);
        
        scheduler.start();
        Thread.sleep(300 * Constants.MILLISECOND);
        assertTrue(callback.getCount() >= 2);
    }

    private static class ExceptionCallback extends CountCallback
    {
        public void run()
        {
            super.run();
            throw new RuntimeException("boo");
        }
    }

    private static class CountCallback implements NullaryProcedure
    {
        private int count = 0;
        public void run()
        {
            count++;
        }

        public int getCount()
        {
            return count;
        }
    }
}
