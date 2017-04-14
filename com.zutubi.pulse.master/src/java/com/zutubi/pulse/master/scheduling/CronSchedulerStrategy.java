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

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.ScheduleBuilder;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

/**
 * <class-comment/>
 */
public class CronSchedulerStrategy extends QuartzSchedulerStrategy
{
    public List<String> canHandle()
    {
        return Arrays.asList(CronTrigger.TYPE);
    }

    protected ScheduleBuilder createScheduleBuilder(Trigger trigger) throws SchedulingException
    {
        CronTrigger cronTrigger = (CronTrigger) trigger;
        try
        {
            return CronScheduleBuilder.cronSchedule(new CronExpression(cronTrigger.getCron()))
                    .withMisfireHandlingInstructionDoNothing();
        }
        catch (ParseException e)
        {
            throw new SchedulingException(e);
        }
    }
}
