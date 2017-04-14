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

import java.util.Arrays;
import java.util.List;

/**
 * This scheduler strategy is for triggers that do nothing.  These
 * types of triggers exist for there configuration only.
 */
public class NoopSchedulerStrategy implements SchedulerStrategy
{
    public List<String> canHandle()
    {
        return Arrays.asList(NoopTrigger.TYPE);
    }

    public void init(Trigger trigger) throws SchedulingException
    {
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
    }

    public void resume(Trigger trigger) throws SchedulingException
    {
    }

    public void stop(boolean force)
    {
    }

    public void schedule(Trigger trigger) throws SchedulingException
    {
    }

    public void unschedule(Trigger trigger) throws SchedulingException
    {
    }

    public void setTriggerHandler(TriggerHandler handler)
    {
    }
}
