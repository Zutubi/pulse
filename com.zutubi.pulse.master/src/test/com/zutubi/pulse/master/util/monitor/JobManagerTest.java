package com.zutubi.pulse.master.util.monitor;

import static com.zutubi.pulse.core.test.TestUtils.executeOnSeparateThread;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class JobManagerTest extends PulseTestCase
{
    private JobManager jobManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        jobManager = new JobManager();
    }

    protected void tearDown() throws Exception
    {
        jobManager = null;

        super.tearDown();
    }

    public void testRegister()
    {
        jobManager.register("test", new NoopJob());

        assertNotNull(jobManager.getJob("test"));
        assertNotNull(jobManager.getMonitor("test"));
    }

    public void testRunJob()
    {
        jobManager.register("test", new NoopJob());

        jobManager.start("test");

        Monitor monitor = jobManager.getMonitor("test");
        assertTrue(monitor.isFinished());
    }

    public void testUnregister()
    {
        jobManager.register("test", new NoopJob());
        assertNotNull(jobManager.getJob("test"));
        assertNotNull(jobManager.getMonitor("test"));

        jobManager.unregister("test");
        assertNull(jobManager.getJob("test"));
        assertNull(jobManager.getMonitor("test"));
    }

    public void testUnregisterStartedJob()
    {
        jobManager.register("test", new NoopJob());
        assertNotNull(jobManager.getJob("test"));
        assertNotNull(jobManager.getMonitor("test"));

        jobManager.start("test");

        assertTrue(jobManager.getMonitor("test").isFinished());

        jobManager.unregister("test");
        assertNull(jobManager.getJob("test"));
        assertNull(jobManager.getMonitor("test"));
    }

    public void testUnregisterRunningJob()
    {
        NeverEndingJob job = new NeverEndingJob();
        jobManager.register("test", job);

        executeOnSeparateThread(new Runnable()
        {
            public void run()
            {
                jobManager.start("test");
            }
        });

        Monitor monitor = jobManager.getMonitor("test");
        while (!monitor.isStarted())
        {
            Thread.yield();
        }
        
        assertTrue(monitor.isStarted());

        try
        {
            jobManager.unregister("test");
            fail();
        }
        catch (IllegalStateException e)
        {
            // expected
        }

        job.endNow();
    }

    private class NoopJob implements Job<Task>
    {
        public Iterator<Task> getTasks()
        {
            return new LinkedList<Task>().iterator();
        }
    }

    private class NeverEndingJob implements Job<Task>
    {
        private NeverEndingTask task = new NeverEndingTask("yawn");

        public void endNow()
        {
            task.endNow();
        }

        public Iterator<Task> getTasks()
        {
            return Arrays.asList((Task)task).iterator();
        }
    }

    private class NeverEndingTask extends AbstractTask
    {
        private boolean running = true;

        public NeverEndingTask(String name)
        {
            super(name);
        }

        public void execute()
        {
            while (true)
            {
                synchronized (this)
                {
                    if (!running)
                    {
                        break;
                    }
                }
                Thread.yield();
            }
        }

        public void endNow()
        {
            synchronized (this)
            {
                this.running = false;            
            }
        }
    }
}
