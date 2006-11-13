package com.zutubi.pulse.scheduling.quartz;

import org.quartz.TriggerListener;
import org.quartz.JobExecutionContext;

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

    /**
     * @see TriggerListener#triggerComplete(org.quartz.Trigger, org.quartz.JobExecutionContext, int)
     */
    public void triggerComplete(org.quartz.Trigger trigger, JobExecutionContext context, int triggerInstructionCode)
    {

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