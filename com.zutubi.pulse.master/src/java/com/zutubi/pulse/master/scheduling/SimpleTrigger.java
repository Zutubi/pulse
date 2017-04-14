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

import java.util.Date;

/**
 * Simple triggers fire at regular time intervals for a specified (possibly
 * infinite) number of repeats.
 */
public class SimpleTrigger extends Trigger
{
    public static final int REPEAT_INDEFINITELY = org.quartz.SimpleTrigger.REPEAT_INDEFINITELY;

    protected static final String TYPE = "simple";

    private Date startTime;
    private long interval;
    private int repeatCount;

    public SimpleTrigger()
    {

    }

    public SimpleTrigger(String name, String group, long interval)
    {
        this(name, group, new Date(), interval, REPEAT_INDEFINITELY);
    }

    /**
     * Creates a trigger that will fire once at the given time.
     *
     * @param name  trigger name (must be unique)
     * @param group trigger group name
     * @param time  the time at which the trigger should fire
     */
    public SimpleTrigger(String name, String group, Date time)
    {
        this(name, group, time, 0, 0);
    }

    /**
     * @param name
     * @param group
     * @param startTime   time at which to start firing
     * @param interval    in milliseconds between successive triggers.
     * @param repeatCount
     */
    public SimpleTrigger(String name, String group, Date startTime, long interval, int repeatCount)
    {
        super(name, group);
        setInterval(interval);
        setRepeatCount(repeatCount);
        setStartTime(startTime);
    }

    public String getType()
    {
        return TYPE;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    private void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public long getInterval()
    {
        return interval;
    }

    private void setInterval(long interval)
    {
        this.interval = interval;
    }

    public int getRepeatCount()
    {
        return repeatCount;
    }

    private void setRepeatCount(int repeatCount)
    {
        this.repeatCount = repeatCount;
    }
}
