package com.zutubi.validation;

import com.zutubi.validation.i18n.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        return new DefaultTextProvider(localeProvider);
    }

    public ValidationAware makeValidationAware(Object o)
    {
        if (o instanceof ValidationAware)
        {
            return (ValidationAware)o;
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

    public List<String> getActionErrors()
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

    public Map getFieldErrors()
    {
        return validationAware.getFieldErrors();
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
