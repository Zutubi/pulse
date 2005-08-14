package com.cinnamonbob.core.config;

import com.opensymphony.xwork.validator.ActionValidatorManager;
import com.opensymphony.xwork.validator.ValidationException;
import com.opensymphony.xwork.validator.ValidatorContext;
import com.opensymphony.xwork.validator.DelegatingValidatorContext;
import com.opensymphony.xwork.ValidationAware;
import com.opensymphony.xwork.LocaleProvider;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: daniel
 * Date: 14/08/2005
 * Time: 11:47:13
 * To change this template use File | Settings | File Templates.
 */
public class CommandValidationManager
{
    //TODO: daniel to clean up.
    public static void validate(Object object) throws ValidationException
    {
        ValidationCallback callback = new ValidationCallback();

        ValidatorContext validatorContext = new DelegatingValidatorContext(
                callback, DelegatingValidatorContext.makeTextProvider(object, callback), callback
        );

        ActionValidatorManager.validate(object, object.getClass().getName(), validatorContext);
        if (callback.hasFieldErrors())
        {
            throw new ValidationException();
        }
    }

    private static class ValidationCallback implements ValidationAware, LocaleProvider
    {
        public Locale getLocale()
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        private Map<String, String> fieldErrors;

        public void setActionErrors(Collection errorMessages)
        {
        }

        public Collection getActionErrors()
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setActionMessages(Collection messages)
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public Collection getActionMessages()
        {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void setFieldErrors(Map errorMap)
        {

            this.fieldErrors = errorMap;

        }

        public Map getFieldErrors()
        {
            if (fieldErrors == null)
            {
                fieldErrors = new HashMap<String, String>();
            }
            return fieldErrors;
        }

        public void addActionError(String anErrorMessage)
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void addActionMessage(String aMessage)
        {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void addFieldError(String fieldName, String errorMessage)
        {
            getFieldErrors().put(fieldName, errorMessage);
        }

        public boolean hasActionErrors()
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean hasActionMessages()
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean hasErrors()
        {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public boolean hasFieldErrors()
        {
            return getFieldErrors().size() > 0;
        }
    }
}
