package com.zutubi.validation;

import com.zutubi.validation.i18n.DefaultTextProvider;
import com.zutubi.validation.i18n.TextProvider;
import com.zutubi.validation.xwork.XWorkValidationAdapter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public void clearFieldErrors()
    {
        validationAware.clearFieldErrors();
    }

    public boolean hasFieldError(String field)
    {
        return validationAware.hasFieldError(field);
    }

    public Map<String, List<String>> getFieldErrors()
    {
        return validationAware.getFieldErrors();
    }

    public void addIgnoredField(String field)
    {
        validationAware.addIgnoredField(field);
    }

    public void addIgnoredFields(Set<String> fields)
    {
        validationAware.addIgnoredFields(fields);
    }

    public void ignoreAllFields()
    {
        validationAware.ignoreAllFields();
    }

    public String getText(String key)
    {
        return textProvider.getText(key);
    }

    public String getText(String key, Object... args)
    {
        return textProvider.getText(key, args);
    }
}
