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

package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.SchedulingException;
import com.zutubi.pulse.master.scheduling.Trigger;

import java.util.LinkedList;
import java.util.List;

/**
 * The actions class for triggers.
 */
public class TriggerConfigurationActions
{
    public static final String ACTION_RESUME = "resume";

    public static final String ACTION_PAUSE = "pause";

    private Scheduler scheduler;

    /**
     * Determine the list of actions that can be applied to the specified trigger.
     * 
     * @param config    trigger in question
     * @return  the list of actions available to the trigger.
     */
    public List<String> getActions(TriggerConfiguration config)
    {
        List<String> actions = new LinkedList<String>();
        Trigger trigger = scheduler.getTrigger(config.getTriggerId());
        if (trigger != null && trigger.isScheduled())
        {
            if (trigger.isPaused())
            {
                actions.add(ACTION_RESUME);
            }
            else
            {
                actions.add(ACTION_PAUSE);
            }
        }
        return actions;
    }

    public void doPause(TriggerConfiguration config) throws SchedulingException
    {
        Trigger trigger = scheduler.getTrigger(config.getTriggerId());
        if (!trigger.isPaused())
        {
            scheduler.pause(trigger);
        }
    }

    public void doResume(TriggerConfiguration config) throws SchedulingException
    {
        Trigger trigger = scheduler.getTrigger(config.getTriggerId());
        if (trigger.isPaused())
        {
            scheduler.resume(trigger);
        }
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
