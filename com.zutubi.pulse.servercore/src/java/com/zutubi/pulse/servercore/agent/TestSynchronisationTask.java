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

package com.zutubi.pulse.servercore.agent;

/**
 * A synchronisation task that exists purely for testing purposes.
 */
public class TestSynchronisationTask implements SynchronisationTask
{
    private boolean succeed;

    public TestSynchronisationTask()
    {
    }

    /**
     * Create a new task for testing.
     *
     * @param succeed if true the task should succeed, if false it should throw
     *        an exception
     */
    public TestSynchronisationTask(boolean succeed)
    {
        this.succeed = succeed;
    }

    public boolean isSucceed()
    {
        return succeed;
    }

    public void execute()
    {
        if (!succeed)
        {
            throw new RuntimeException("Test failure.");
        }
    }
}