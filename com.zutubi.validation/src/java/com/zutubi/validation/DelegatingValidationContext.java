package com.zutubi.validation;

import com.zutubi.validation.i18n.*;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * <class-comment/>
 */
public class DelegatingValidationContext implements ValidationContext
{
    protected ValidationAware validationAware;
    protected TextProvider textProvider;

    public DelegatingValidationContext(ValidationAware validationAware, TextProvider textProvider)
    {
        this.validationAware = validationAware;
        this.textProvider = textProvider;
    }

    public DelegatingValidationContext(Object obj)
    {
        validationAware = makeValidationAware(obj);
        textProvider = makeTextProvider(obj);
    }

    protected DelegatingValidationContext()
    {

    }

    public TextProvider makeTextProvider(Object obj)
    {
        if (obj instanceof TextProvider)
        {
            return ((TextProvider)obj).getTextProvider(obj);
        }
        return new DefaultTextProvider();
    }

    public ValidationAware makeValidationAware(Object o)
    {
        if (o instanceof ValidationAware)
        {
            return (ValidationAware)o;
        }
        else if (o instanceof com.opensymphony.xwork.ValidationAware)
        {
            return new XWorkValidationAdapter((com.opensymphony.xwork.ValidationAware)o);
        }
        return new ValidationAwareSupport();
    }

    public TextProvider getTextProvider(Object context)
    {
        return textProvider.getTextProvider(context);
    }

    public void addActionError(String error)
    {
        validationAware.addActionError(error);
    }

    public void addFieldError(String field, String error)
    {
        validationAware.addFieldError(field, error);
    }

    public Collection<String> getActionErrors()
    {
        return validationAware.getActionErrors();
    }

    public List<String> getFieldErrors(String field)
    {
        return validationAware.getFieldErrors(field);
    }

    public boolean hasErrors()
    {
        return validationAware.hasErrors();
    }

    public boolean hasFieldErrors()
    {
        return validationAware.hasFieldErrors();
    }

    public boolean hasActionErrors()
    {
        return validationAware.hasActionErrors();
    }

    public boolean hasFieldError(String field)
    {
        return validationAware.hasFieldError(field);
    }

    public Map getFieldErrors()
    {
        return validationAware.getFieldErrors();
    }

    public void addActionMessage(String message)
    {
        validationAware.addActionMessage(message);
    }

    public Collection<String> getActionMessages()
    {
        return validationAware.getActionMessages();
    }

    public boolean hasActionMessages()
    {
        return validationAware.hasActionMessages();
    }

    public void setActionMessages(Collection<String> messages)
    {
        validationAware.setActionMessages(messages);
    }

    public void setActionErrors(Collection<String> errors)
    {
        validationAware.setActionErrors(errors);
    }

    public void setFieldErrors(Map<String, List<String>> errors)
    {
        validationAware.setFieldErrors(errors);
    }

    public String getText(String key)
    {
        return textProvider.getText(key);
    }

    public String getText(String key, String defaultValue)
    {
        return textProvider.getText(key, defaultValue);
    }

    public String getText(String key, Object... args)
    {
        return textProvider.getText(key, args);
    }

    public String getText(String key, String defaultValue, Object... args)
    {
        return textProvider.getText(key, defaultValue, args);
    }

    public String getFullFieldName(String fieldName)
    {
        return fieldName;
    }
}
