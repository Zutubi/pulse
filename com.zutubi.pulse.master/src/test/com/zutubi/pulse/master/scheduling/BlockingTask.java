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

package com.zutubi.pulse.master.scheduling;

/**
 * <class-comment/>
 */
public class BlockingTask implements Task
{
    private static final Object lock = new Object();
    private static boolean waiting = false;

    public void execute(TaskExecutionContext context)
    {
        synchronized(lock)
        {
            DefaultTriggerHandlerTest.stopWaiting();
            waiting = true;
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
}
