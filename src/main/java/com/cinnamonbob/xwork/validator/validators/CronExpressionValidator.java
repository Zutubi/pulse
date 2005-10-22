package com.cinnamonbob.xwork.validator.validators;

import com.opensymphony.xwork.validator.validators.FieldValidatorSupport;
import com.opensymphony.xwork.validator.ValidationException;

import java.text.ParseException;

import org.quartz.CronTrigger;

/**
 * <class-comment/>
 */
public class CronExpressionValidator extends FieldValidatorSupport
{

    public void validate(Object object) throws ValidationException
    {
        Object obj = getFieldValue(getFieldName(), object);
        try
        {
            new CronTrigger("triggerName", null, (String)obj);
        }
        catch (ParseException e)
        {
            addFieldError(getFieldName(), e.getMessage());
        }
    }
}