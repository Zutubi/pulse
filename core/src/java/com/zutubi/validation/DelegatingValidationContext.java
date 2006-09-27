package com.zutubi.validation;

import com.zutubi.validation.i18n.*;
import com.zutubi.pulse.i18n.MessagesTextProvider;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Collection;

/**
 * <class-comment/>
 */
public class DelegatingValidationContext implements ValidationContext
{
    protected ValidationAware validationAware;
    protected LocaleProvider localeProvider;
    protected TextProvider textProvider;

    public DelegatingValidationContext(ValidationAware validationAware, LocaleProvider localeProvider, TextProvider textProvider)
    {
        this.validationAware = validationAware;
        this.localeProvider = localeProvider;
        this.textProvider = textProvider;
    }

    public DelegatingValidationContext(Object obj)
    {
        validationAware = makeValidationAware(obj);
        localeProvider = makeLocaleProvider(obj);
        textProvider = makeTextPovider(obj, localeProvider);
    }

    protected DelegatingValidationContext()
    {

    }

    public LocaleProvider makeLocaleProvider(Object obj)
    {
        if (obj instanceof LocaleProvider)
        {
            return (LocaleProvider) obj;
        }
        return new DefaultLocaleProvider();
    }

    public TextProvider makeTextPovider(Object obj, LocaleProvider localeProvider)
    {
        if (obj instanceof TextProvider)
        {
            return (TextProvider) obj;
        }
        // this is no good.... we need a text provider that behaves in the same way as the
        // systems text provider, ie our MessageTextProvider... but how do we inject it?
        return new MessagesTextProvider(obj);
//        return new DefaultTextProvider(localeProvider);
    }

    public ValidationAware makeValidationAware(Object o)
    {
        if (o instanceof ValidationAware)
        {
            return (ValidationAware)o;
        }
        else if (o instanceof com.opensymphony.xwork.ValidationAware)
        {
            return new XWorkValidationAwareBridge((com.opensymphony.xwork.ValidationAware)o);
        }
        return new ValidationAwareSupport();
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

    public Locale getLocale()
    {
        return localeProvider.getLocale();
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
