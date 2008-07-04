package com.zutubi.validation;

import com.zutubi.validation.i18n.TextProvider;
import com.zutubi.validation.i18n.DefaultTextProvider;

import java.util.Set;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.HashMap;

/**
 *
 *
 */
public class MockValidationContext implements ValidationContext
{
    private boolean ignoreAllFields = false;

    private Set<String> ignoredFields = new HashSet<String>();

    private List<String> actionErrors = new LinkedList<String>();

    private Map<String, List<String>> fieldErrors = new HashMap<String, List<String>>();

    public void addIgnoredField(String field)
    {
        ignoredFields.add(field);
    }

    public void addIgnoredFields(Set<String> fields)
    {
        ignoredFields.addAll(fields);
    }

    public void ignoreAllFields()
    {
        ignoreAllFields = true;
    }

    public void addActionError(String error)
    {
        actionErrors.add(error);
    }

    public void addFieldError(String field, String error)
    {
        getFieldErrors(field).add(error);
    }

    public Collection<String> getActionErrors()
    {
        return actionErrors;
    }

    public List<String> getFieldErrors(String field)
    {
        List<String> errors = fieldErrors.get(field);
        if (errors == null)
        {
            errors = new LinkedList<String>();
            fieldErrors.put(field, errors);
        }
        return errors;
    }

    public Map<String, List<String>> getFieldErrors()
    {
        return fieldErrors;
    }

    public boolean hasErrors()
    {
        return hasActionErrors() || hasFieldErrors();
    }

    public boolean hasFieldErrors()
    {
        for (List<String> errors : fieldErrors.values())
        {
            if (errors.size() > 0)
            {
                return true;
            }
        }
        return false;
    }

    public boolean hasFieldError(String field)
    {
        return getFieldErrors(field).size() > 0;
    }

    public boolean hasActionErrors()
    {
        return actionErrors.size() > 0;
    }

    public void clearFieldErrors()
    {
        fieldErrors.clear();
    }

    public String getText(String key)
    {
        return key;
    }

    public String getText(String key, Object... args)
    {
        return key;
    }

    public TextProvider getTextProvider(Object context)
    {
        return new DefaultTextProvider();
    }
}
