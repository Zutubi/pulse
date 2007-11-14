package com.zutubi.pulse.prototype.config.project.triggers;

import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorSupport;
import org.quartz.CronTrigger;
import org.quartz.impl.calendar.BaseCalendar;

/**
 */
public class CronExpressionValidator extends FieldValidatorSupport
{
    public void validate(Object object) throws ValidationException
    {
        Object obj = getFieldValue(getFieldName(), object);
        if (obj != null && obj instanceof String)
        {
            String expression = (String) obj;

            if (expression.length() > 0)
            {
                try
                {
                    new CronTrigger("triggerName", null, expression).computeFirstFireTime(new BaseCalendar());
                }
                catch (Exception e)
                {
                    setDefaultMessage(e.getMessage());
                    addFieldError(getFieldName());
                }
            }
        }
    }
}