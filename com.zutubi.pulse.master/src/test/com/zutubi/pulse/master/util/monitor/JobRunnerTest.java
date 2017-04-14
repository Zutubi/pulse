/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.util.monitor;

import com.zutubi.pulse.core.test.TestUtils;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.Constants;

public class JobRunnerTest extends PulseTestCase
{
    private JobRunner<Task> runner = null;

    protected void setUp() throws Exception
    {
        super.setUp();

        runner = new JobRunner<Task>();
    }

    protected void tearDown() throws Exception
    {
        runner = null;

        super.tearDown();
    }

    public void testSuccessfulTask()
    {
        Task success = new SuccessfulTask();
        runner.run(success);

        Monitor monitor = runner.getMonitor();
        assertTrue(monitor.isStarted());
        assertTrue(monitor.isFinished());
        assertFalse(monitor.isFailed());
    }

    public void testFailedTask()
    {
        Task failed = new FailedTask();
        runner.run(failed);

        Monitor monitor = runner.getMonitor();
        assertTrue(monitor.isStarted());
        assertTrue(monitor.isFinished());
        assertFalse(monitor.isFailed());
    }

    public void testHaltOnFailure()
    {
        Task failed = new HaltOnFailureTask();
        Task successful = new SuccessfulTask();
        runner.run(failed, successful);

        Monitor<Task> monitor = runner.getMonitor();
        assertTrue(monitor.isStarted());
        assertTrue(monitor.isFinished());
        assertTrue(monitor.isFailed());

        assertTrue(monitor.getProgress(failed).isFailed());
        assertTrue(monitor.getProgress(successful).isAborted());
    }

    public void testContinueOnFailure()
    {
        Task failed = new ContinueOnFailureTask();
        Task successful = new SuccessfulTask();
        runner.run(failed, successful);

        Monitor<Task> monitor = runner.getMonitor();
        assertTrue(monitor.isStarted());
        assertTrue(monitor.isFinished());
        assertFalse(monitor.isFailed());

        assertTrue(monitor.getProgress(failed).isFailed());
        assertTrue(monitor.getProgress(successful).isSuccessful());
    }

    public void testSingleCompletedTask()
    {
        runner.run(new SuccessfulTask());

        Monitor monitor = runner.getMonitor();
        
        assertEquals(100, monitor.getPercentageComplete());
        assertEquals(1, monitor.getCompletedTasks());
    }

    public void testMultipleCompletedTasks()
    {
        runner.run(new SuccessfulTask(), new SuccessfulTask());

        Monitor monitor = runner.getMonitor();

        assertEquals(100, monitor.getPercentageComplete());
        assertEquals(2, monitor.getCompletedTasks());
    }

    public void testLastTaskFailed()
    {
        runner.run(new SuccessfulTask(), new SuccessfulTask(), new HaltOnFailureTask());

        Monitor monitor = runner.getMonitor();
        assertTrue(monitor.isFailed());
    }

    // this test works through the virtue of the correct behaviour of the monitoring.  If something
    // breaks, then this test will hang until it times out.
    // Im sure there is a cleaner way to do this using locks, but at least this is very clear on what is happening
    // and is working through the monitor interface, in the same way that actual monitoring would.
    public void testIncrementalChangesInFeedbackOfRunningTask() throws InterruptedException
    {
        final boolean[] success = new boolean[]{false};
        
        TestUtils.executeOnSeparateThreadAndWait(new Runnable()
        {
            public void run()
            {
                final RemoteControlTask task = new RemoteControlTask();

                TestUtils.executeOnSeparateThread(new Runnable()
                {
                    public void run()
                    {
                        runner.run(task);
                    }
                });

                // give the runner a chance to start.

                Monitor<Task> monitor = runner.getMonitor();
                TaskFeedback progress = monitor.getProgress(task);
                while (progress == null)
                {
                    Thread.yield();
                    progress = monitor.getProgress(task);
                }

                // wait until it has started.  check point a.
                while (!monitor.getProgress(task).isStarted())
                {
                    // noop.
                    Thread.yield();
                }

                assertTrue(monitor.isStarted());
                assertFalse(monitor.isFinished());
                assertTrue(monitor.getProgress(task).isStarted());

                task.complete();

                // wait until it has completed.  check point b.
                while (!monitor.getProgress(task).isFinished())
                {
                    // noop.
                    Thread.yield();
                }

                while (!monitor.isFinished())
                {
                    Thread.yield();
                }

                assertTrue(monitor.isFinished());
                assertTrue(monitor.getProgress(task).isFinished());
                success[0] = true;
            }
        }, 2 * Constants.SECOND);

        assertTrue("Test timed out.", success[0]);
    }

    private class SuccessfulTask extends AbstractTask
    {
        public SuccessfulTask()
        {
            super("successful");
        }
    }

    private class FailedTask extends AbstractTask
    {
        public FailedTask()
        {
            super("failed");
        }

        public boolean hasFailed()
        {
            return true;
        }
    }

    private class HaltOnFailureTask extends AbstractTask
    {
        public HaltOnFailureTask()
        {
            super("haltonfailure");
        }

        public boolean hasFailed()
        {
            return true;
        }

        public boolean haltOnFailure()
        {
            return true;
        }
    }

    private class ContinueOnFailureTask extends AbstractTask
    {
        public ContinueOnFailureTask()
        {
            super("continueonfailure");
        }

        public boolean hasFailed()
        {
            return true;
        }

        public boolean haltOnFailure()
        {
            return false;
        }
    }

    private class RemoteControlTask extends AbstractTask
    {
        private boolean complete = false;

        public RemoteControlTask()
        {
            super("remoteControlTask");
        }

        public void execute()
        {
            while (!complete)
            {
                Thread.yield();
            }
        }

        public void complete()
        {
            complete = true;
        }
    }
}
