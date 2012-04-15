package com.zutubi.pulse.master.util.monitor;

import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.time.TestClock;

public class TaskFeedbackTest extends PulseTestCase
{
    private JobMonitor monitor;
    private TestClock clock;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        monitor = new JobMonitor();
        clock = new TestClock();
    }

    public void testElapsedTime()
    {
        Task noopTask = new NoopTask();
        TaskFeedback feedback = new TaskFeedback<Task>(monitor, noopTask);
        feedback.setClock(clock);

        assertEquals(TaskFeedback.UNDEFINED, feedback.getElapsedTime());
        feedback.markStarted();
        clock.setTime(100);
        assertEquals(100, feedback.getElapsedTime());
        clock.setTime(200);
        assertEquals(200, feedback.getElapsedTime());

        feedback.markSuccessful();
        assertEquals(200, feedback.getElapsedTime());
        clock.setTime(300);
        assertEquals(200, feedback.getElapsedTime());
    }
}
