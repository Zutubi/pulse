package com.zutubi.pulse.master.scheduling;

import com.zutubi.util.Constants;
import com.zutubi.util.NullaryProcedure;
import com.zutubi.util.bean.WiringObjectFactory;
import com.zutubi.util.junit.ZutubiTestCase;
import static com.zutubi.pulse.master.scheduling.CallbackService.CALLBACK_TRIGGER_GROUP;
import static org.mockito.Mockito.*;

import java.util.Date;

public class CallbackServiceTest extends ZutubiTestCase
{
    private Scheduler scheduler;
    private CallbackService callbackService;
    private WiringObjectFactory objectFactory;

    public void setUp() throws Exception
    {
        super.setUp();

        objectFactory = new WiringObjectFactory();
        scheduler = mock(Scheduler.class);

        objectFactory.initProperties(this);

        callbackService = objectFactory.buildBean(CallbackService.class);

        objectFactory.initProperties(this);
    }

    public void testCallbacksAreNotPersistent() throws Exception
    {
        CountCallback callback = new CountCallback();
        callbackService.registerCallback(callback, 100);

        verify(scheduler, times(1)).schedule((Trigger) anyObject());
    }

    public void testCallbackInterval() throws Exception
    {
        CountCallback callback = new CountCallback();
        callbackService.registerCallback(callback, 100);

        verify(scheduler, times(1)).schedule((Trigger) anyObject());
    }

    public void testCallbackDate() throws Exception
    {
        Date time = new Date(System.currentTimeMillis() + (200 * Constants.MILLISECOND));
        CountCallback callback = new CountCallback();
        callbackService.registerCallback(callback, time);

        verify(scheduler, times(1)).schedule((Trigger) anyObject());
    }

    public void testCallbackThrowingException() throws Exception
    {
        // register the callback
        ExceptionCallback callback = new ExceptionCallback();
        callbackService.registerCallback(callback, 1);
        verify(scheduler, times(1)).schedule((Trigger) anyObject());

        // manually execute the task (as would happen when the scheduler strategy fires)
        CallbackService.CallbackTask task = objectFactory.buildBean(CallbackService.CallbackTask.class);
        TaskExecutionContext context = mock(TaskExecutionContext.class);
        stub(context.getTrigger()).toReturn(new NoopTrigger("1", CALLBACK_TRIGGER_GROUP));

        task.execute(context);
    }

    public void testCallback() throws SchedulingException
    {
        // register the callback
        CountCallback callback = new CountCallback();
        callbackService.registerCallback(callback, 1);
        verify(scheduler, times(1)).schedule((Trigger) anyObject());

        // manually execute the task (as would happen when the scheduler strategy fires)
        CallbackService.CallbackTask task = objectFactory.buildBean(CallbackService.CallbackTask.class);
        TaskExecutionContext context = mock(TaskExecutionContext.class);
        stub(context.getTrigger()).toReturn(new NoopTrigger("1", CALLBACK_TRIGGER_GROUP));

        task.execute(context);

        assertEquals(1, callback.getCount());
    }

    public void testUnregisterCallback() throws Exception
    {
        CountCallback callback = new CountCallback();
        assertFalse(callbackService.unregisterCallback(callback));
        verify(scheduler, never()).unschedule((Trigger) anyObject());

        callbackService.registerCallback(callback, 100);
        stub(scheduler.getTrigger("1", CALLBACK_TRIGGER_GROUP)).toReturn(new NoopTrigger("1", CALLBACK_TRIGGER_GROUP));

        assertTrue(callbackService.unregisterCallback(callback));
        verify(scheduler, times(1)).unschedule((Trigger) anyObject());
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
