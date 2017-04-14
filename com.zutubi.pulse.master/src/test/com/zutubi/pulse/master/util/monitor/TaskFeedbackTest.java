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
