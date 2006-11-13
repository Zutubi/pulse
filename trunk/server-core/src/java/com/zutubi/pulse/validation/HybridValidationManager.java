package com.zutubi.pulse.validation;

import com.opensymphony.xwork.validator.ActionValidatorManager;
import com.opensymphony.xwork.validator.ValidatorContext;
import com.opensymphony.xwork.validator.ActionValidatorManagerFactory;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.ValidationManager;
import com.zutubi.i18n.locale.LocaleManager;

import java.util.*;

/**
 * <class comment/>
 */
public class HybridValidationManager implements ValidationManager
{
    private ValidationManager zutubiValidationManager;
    private ActionValidatorManager xworkValidationManager;

    public HybridValidationManager()
    {
        this.xworkValidationManager = ActionValidatorManagerFactory.getInstance();
    }

    public void validate(Object o, ValidationContext context) throws ValidationException
    {
        zutubiValidationManager.validate(o, context);

        // Do not continue if validation errors have been detected. This is to ensure that
        // duplicate validation rules are not represented in the validation context.
        if (context.hasErrors())
        {
            return;
        }

        try
        {
            xworkValidationManager.validate(o, null, new ValidatorContextAdapter(context));
        }
        catch (com.opensymphony.xwork.validator.ValidationException e)
        {
            throw new ValidationException(e.getMessage(), e);
        }
    }

    public void setValidationManager(ValidationManager validationManager)
    {
        this.zutubiValidationManager = validationManager;
    }

    private class ValidatorContextAdapter implements ValidatorContext
    {
        private ValidationContext delegate;

        public ValidatorContextAdapter(ValidationContext delegate)
        {
            this.delegate = delegate;
        }

        public String getFullFieldName(String fieldName)
        {
            return delegate.getFullFieldName(fieldName);
        }

        public void setActionErrors(Collection errorMessages)
        {
            delegate.setActionErrors(errorMessages);
        }

        public Collection getActionErrors()
        {
            return delegate.getActionErrors();
        }

        public void setActionMessages(Collection messages)
        {
            delegate.setActionMessages(messages);
        }

        public Collection getActionMessages()
        {
            return delegate.getActionMessages();
        }

        public void setFieldErrors(Map errorMap)
        {
            delegate.setFieldErrors(errorMap);
        }

        public Map getFieldErrors()
        {
            return delegate.getFieldErrors();
        }

        public void addActionError(String anErrorMessage)
        {
            delegate.addActionError(anErrorMessage);
        }

        public void addActionMessage(String aMessage)
        {
            delegate.addActionMessage(aMessage);
        }

        public void addFieldError(String fieldName, String errorMessage)
        {
            delegate.addFieldError(fieldName, errorMessage);
        }

        public boolean hasActionErrors()
        {
            return delegate.hasErrors();
        }

        public boolean hasActionMessages()
        {
            return delegate.hasActionMessages();
        }

        public boolean hasErrors()
        {
            return delegate.hasErrors();
        }

        public boolean hasFieldErrors()
        {
            return delegate.hasFieldErrors();
        }

        public String getText(String key)
        {
            return delegate.getText(key);
        }

        public String getText(String key, String defaultValue)
        {
            return delegate.getText(key, defaultValue);
        }

        public String getText(String key, List args)
        {
            return delegate.getText(key, args.toArray());
        }

        public String getText(String key, String defaultValue, List args)
        {
            return delegate.getText(key, defaultValue, args.toArray());
        }

        public String getText(String key, String defaultValue, List args, OgnlValueStack stack)
        {
            return getText(key, defaultValue, args);
        }

        public ResourceBundle getTexts(String bundleName)
        {
            return null;
        }

        public ResourceBundle getTexts()
        {
            return null;
        }

        public Locale getLocale()
        {
            return LocaleManager.getManager().getLocale();
        }
    }
}
