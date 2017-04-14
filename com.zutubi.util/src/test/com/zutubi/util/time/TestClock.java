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

package com.zutubi.util.time;

/**
 * Implementation of {@link Clock} that allows you to control the time.
 */
public class TestClock implements Clock
{
    private long time;

    public TestClock()
    {
        this(0);
    }

    public TestClock(long time)
    {
        this.time = time;
    }

    public void add(long duration)
    {
        time += duration;
    }

    public void setTime(long time)
    {
        this.time = time;
    }

    public long getCurrentTimeMillis()
    {
        return time;
    }
}
