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
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.util.EnumUtils;

/**
 * Format state for trigger instances.
 */
public class TriggerConfigurationStateDisplay
{
    private Scheduler scheduler;

    public String formatState(TriggerConfiguration config)
    {
        Trigger trigger = scheduler.getTrigger(config.getTriggerId());
        if (trigger == null)
        {
            return "[invalid trigger]";
        }
        else
        {
            return EnumUtils.toPrettyString(trigger.getState());
        }
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
