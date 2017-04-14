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

package com.zutubi.util.concurrent;

import com.zutubi.util.junit.ZutubiTestCase;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ConcurrentUtilsTest extends ZutubiTestCase
{
    private static final Integer TASK_RESULT    = 0;
    private static final Integer DEFAULT_RESULT = 1;

    public void testRunWithTimeoutTaskCompletes()
    {
        Integer result = ConcurrentUtils.runWithTimeout(new Callable<Integer>()
        {
            public Integer call() throws Exception
            {
                return TASK_RESULT;
            }
        }, 10, TimeUnit.SECONDS, DEFAULT_RESULT);

        assertEquals(TASK_RESULT, result);
    }

    public void testRunWithTimeoutTaskHangs()
    {
        Integer result = ConcurrentUtils.runWithTimeout(new Callable<Integer>()
        {
            public Integer call() throws Exception
            {
                Thread.sleep(1000);
                return TASK_RESULT;
            }
        }, 1, TimeUnit.MILLISECONDS, DEFAULT_RESULT);

        assertEquals(DEFAULT_RESULT, result);
    }

    public void testRunWithTimeoutTaskThrows()
    {
        try
        {
            ConcurrentUtils.runWithTimeout(new Callable<Integer>()
            {
                public Integer call() throws Exception
                {
                    throw new Exception("ouch");
                }
            }, 10, TimeUnit.SECONDS, DEFAULT_RESULT);

            fail("Task-thrown exception should have been propagated as a RuntimeException");
        }
        catch (RuntimeException e)
        {
            assertTrue(e.getMessage().contains("ouch"));
        }
    }
}
