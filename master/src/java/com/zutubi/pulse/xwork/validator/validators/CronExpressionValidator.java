package com.zutubi.pulse.xwork.validator.validators;

import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.xwork.validator.validators.FieldValidatorSupport;
import org.quartz.CronTrigger;
import org.quartz.impl.calendar.BaseCalendar;

/**
 * <class-comment/>
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
                    addFieldError(getFieldName(), object);
                }
            }
        }
    }
}