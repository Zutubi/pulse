package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.StringFieldValidatorSupport;
import org.quartz.CronScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.impl.calendar.BaseCalendar;
import org.quartz.spi.OperableTrigger;

/**
 */
public class CronExpressionValidator extends StringFieldValidatorSupport
{
    public void validateStringField(String expression) throws ValidationException
    {
        if (expression == null)
        {
            expression = "";
        }
        try
        {
            OperableTrigger trigger = (OperableTrigger) TriggerBuilder.newTrigger()
                    .withSchedule(CronScheduleBuilder.cronSchedule(expression))
                    .build();
            trigger.computeFirstFireTime(new BaseCalendar());
        }
        catch (Exception e)
        {
            addErrorMessage(e.getMessage());
        }
    }
}