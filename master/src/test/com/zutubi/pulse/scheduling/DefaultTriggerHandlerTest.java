package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.scheduling.persistence.mock.MockTriggerDao;
import com.zutubi.pulse.test.PulseTestCase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <class-comment/>
 */
public class DefaultTriggerHandlerTest extends PulseTestCase
{
    private DefaultTriggerHandler handler;
    private MockTriggerDao triggerDao;
    private ExecutorService executor;

    public DefaultTriggerHandlerTest()
    {
    }

    public DefaultTriggerHandlerTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        handler = new DefaultTriggerHandler();
        handler.setObjectFactory(new ObjectFactory());
        triggerDao = new MockTriggerDao();

        executor = Executors.newFixedThreadPool(5);
    }

    protected void tearDown() throws Exception
    {
        executor.shutdown();
        handler = null;
        triggerDao = null;

        super.tearDown();
    }

    public void testTriggerDoesNotFireConcurrently() throws SchedulingException
    {
        Trigger a = new NoopTrigger("a", "a");
        a.setTaskClass(PauseTestTask.class);
        triggerDao.save(a);

        assertEquals(0, a.getTriggerCount());
        fire(a);
        pause(100);

        assertEquals(1, a.getTriggerCount());
        fire(a);
        assertEquals(1, a.getTriggerCount());
        fire(a);
        assertEquals(1, a.getTriggerCount());

        PauseTestTask.unpause();

        assertEquals(1, a.getTriggerCount());
        fire(a);
        pause(100);

        assertEquals(2, a.getTriggerCount());

        PauseTestTask.unpause();
    }

    private void fire(final Trigger trigger)
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

    private void pause(long milliseconds)
    {
        try
        {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException e)
        {
        }
    }
}


