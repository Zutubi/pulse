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

import com.zutubi.pulse.master.scheduling.CronTrigger;
import com.zutubi.pulse.master.scheduling.Scheduler;
import com.zutubi.pulse.master.scheduling.SchedulingException;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.util.logging.Logger;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.annotations.Required;

/**
 * Used to configure a trigger that is defined by a Cron-like expression.
 */
@SymbolicName("zutubi.cronTriggerConfig")
@Form(fieldOrder = {"name", "cron", "pauseAfterFiring"})
@Wire
public class CronBuildTriggerConfiguration extends FireableTriggerConfiguration
{
    private static final Logger LOG = Logger.getLogger(CronBuildTriggerConfiguration.class);

    @Required
    @Constraint("CronExpressionValidator")
    private String cron;
    private boolean pauseAfterFiring;
    private Scheduler scheduler;

    public String getCron()
    {
        return cron;
    }

    public void setCron(String cron)
    {
        this.cron = cron;
    }

    public boolean isPauseAfterFiring()
    {
        return pauseAfterFiring;
    }

    public void setPauseAfterFiring(boolean pauseAfterFiring)
    {
        this.pauseAfterFiring = pauseAfterFiring;
    }

    public Trigger newTrigger()
    {
        return new CronTrigger(cron, getName());
    }

    public void update(Trigger trigger)
    {
        super.update(trigger);
        CronTrigger cronTrigger = (CronTrigger) trigger;
        cronTrigger.setCron(cron);
    }

    @Override
    public void postFire(Trigger trigger)
    {
        super.postFire(trigger);
        if (pauseAfterFiring)
        {
            try
            {
                scheduler.pause(trigger);
            }
            catch (SchedulingException e)
            {
                LOG.severe(e);
            }
        }
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }
}
