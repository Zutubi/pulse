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

package com.zutubi.pulse.master.scheduling.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

/**
 * Basic implementation of the TriggerListener interface that will allow extensions
 * to selectively implement parts of the interface.
 */
public class TriggerAdapter implements TriggerListener
{
    public String getName()
    {
        return "TriggerAdapter";
    }

    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode)
    {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * @see TriggerListener#triggerFired(org.quartz.Trigger, org.quartz.JobExecutionContext)
     */
    public void triggerFired(org.quartz.Trigger trigger, JobExecutionContext context)
    {

    }

    /**
     * @see TriggerListener#triggerMisfired(org.quartz.Trigger)
     */
    public void triggerMisfired(org.quartz.Trigger trigger)
    {

    }

    /**
     * @see TriggerListener#vetoJobExecution(org.quartz.Trigger, org.quartz.JobExecutionContext)
     */
    public boolean vetoJobExecution(org.quartz.Trigger trigger, JobExecutionContext context)
    {
        return false;
    }
}