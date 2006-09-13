package com.zutubi.validation;

import com.zutubi.validation.i18n.TextProvider;
import com.zutubi.validation.i18n.NoopTextProvider;
import com.zutubi.validation.i18n.LocaleProvider;
import com.zutubi.validation.i18n.DefaultLocalProvider;

import java.util.List;
import java.util.Locale;

/**
 * <class-comment/>
 */
public class DelegatingValidationContext implements ValidationContext
{
    private ValidationAware validationAware;
    private LocaleProvider localeProvider;
    private TextProvider textProvider;

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
        textProvider = makeTextPovider(obj);
    }

    public static LocaleProvider makeLocaleProvider(Object obj)
    {
        if (obj instanceof LocaleProvider)
        {
            return (LocaleProvider) obj;
        }
        return new DefaultLocalProvider();
    }

    public static TextProvider makeTextPovider(Object obj)
    {
        if (obj instanceof TextProvider)
        {
            return (TextProvider) obj;
        }
        return new NoopTextProvider();
    }

    public static ValidationAware makeValidationAware(Object o)
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

    public Locale getLocale()
    {
        return localeProvider.getLocale();
    }

    public String getText(String txt)
    {
        return textProvider.getText(txt);
    }

    public String getFullFieldName(String fieldName)
    {
        return fieldName;
    }
}
