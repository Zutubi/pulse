package com.cinnamonbob.scheduling.quartz;

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

    public void triggerComplete(org.quartz.Trigger trigger, JobExecutionContext context, int triggerInstructionCode)
    {

    }

    public void triggerFired(org.quartz.Trigger trigger, JobExecutionContext context)
    {

    }

    public void triggerMisfired(org.quartz.Trigger trigger)
    {

    }

    public boolean vetoJobExecution(org.quartz.Trigger trigger, JobExecutionContext context)
    {
        return false;
    }
}