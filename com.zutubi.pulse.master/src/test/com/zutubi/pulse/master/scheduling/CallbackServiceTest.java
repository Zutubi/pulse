package com.zutubi.pulse.master.scheduling;

import static com.zutubi.pulse.master.scheduling.CallbackService.CALLBACK_TRIGGER_GROUP;
import com.zutubi.util.Constants;
import com.zutubi.util.bean.WiringObjectFactory;
import com.zutubi.util.junit.ZutubiTestCase;
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
        ExceptionCallback callback = new ExceptionCallback();
        callbackService.registerCallback(callback, 1);

        try
        {
            triggerCallback("1");
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    public void testCallback() throws SchedulingException
    {
        CountCallback callback = new CountCallback();
        callbackService.registerCallback(callback, 1);
        verify(scheduler, times(1)).schedule((Trigger) anyObject());

        triggerCallback("1");

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

    public void testNamedCallback() throws SchedulingException
    {
        CountCallback callback = new CountCallback();
        callbackService.registerCallback("callback", callback, 1);
        verify(scheduler, times(1)).schedule((Trigger) anyObject());

        assertEquals(callback, callbackService.getCallback("callback"));

        triggerCallback("callback");
        assertEquals(1, callback.getCount());
        
        assertTrue(callbackService.unregisterCallback("callback"));
        assertNull(callbackService.getCallback("callback"));
    }

    public void testMultipleCallbacksDistinct()
    {
        CountCallback callbackA = new CountCallback();
        callbackService.registerCallback("a", callbackA, 1);
        CountCallback callbackB = new CountCallback();
        callbackService.registerCallback("b", callbackB, 1);

        assertEquals(0, callbackA.getCount());
        assertEquals(0, callbackB.getCount());

        triggerCallback("a");

        assertEquals(1, callbackA.getCount());
        assertEquals(0, callbackB.getCount());
    }

    // manually execute the task (as would happen when the scheduler strategy fires)
    private void triggerCallback(String name)
    {
        CallbackService.CallbackTask task = objectFactory.buildBean(CallbackService.CallbackTask.class);
        TaskExecutionContext context = mock(TaskExecutionContext.class);
        stub(context.getTrigger()).toReturn(new NoopTrigger(name, CALLBACK_TRIGGER_GROUP));

        task.execute(context);
    }

    private static class ExceptionCallback extends CountCallback
    {
        public void run()
        {
            super.run();
            throw new RuntimeException("We made this.");
        }
    }

    private static class CountCallback implements Runnable
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
