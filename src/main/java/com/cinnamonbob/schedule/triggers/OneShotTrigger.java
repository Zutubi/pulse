package com.cinnamonbob.schedule.triggers;

import com.cinnamonbob.schedule.SchedulingException;

import java.util.Date;

/**
 * <class-comment/>
 */
public class OneShotTrigger extends VariableCronTrigger
{
    public void trigger()
    {
        super.trigger();
    }

    public Date getNextTriggerTime() throws SchedulingException
    {
        if (getTriggerCount() == 0)
        {
            return super.getNextTriggerTime();
        }
        return null;
    }

}
