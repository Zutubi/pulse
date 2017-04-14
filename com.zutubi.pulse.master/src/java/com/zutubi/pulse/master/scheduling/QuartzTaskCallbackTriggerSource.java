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

import com.zutubi.pulse.master.model.persistence.TriggerDao;

/**
 * This trigger source is stored in the quartz trigger data map,
 * and provides a handle to the Pulse trigger.
 *
 * If the trigger has a persistent state, then a fresh instance
 * is loaded via the {@link TriggerDao#findById(long)} so that this
 * trigger can correctly participate in any active transactions.
 */
public class QuartzTaskCallbackTriggerSource
{
    private TriggerDao triggerDao;

    private Trigger trigger;

    public QuartzTaskCallbackTriggerSource(Trigger trigger)
    {
        this.trigger = trigger;
    }

    public Trigger getTrigger()
    {
        if (this.trigger.isPersistent())
        {
            return triggerDao.findById(this.trigger.getId());
        }
        else
        {
            return this.trigger;
        }
    }

    public void setTriggerDao(TriggerDao triggerDao)
    {
        this.triggerDao = triggerDao;
    }
}
