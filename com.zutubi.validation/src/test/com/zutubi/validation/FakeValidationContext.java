package com.zutubi.validation;

import com.zutubi.util.config.Config;
import com.zutubi.util.config.PropertiesConfig;
import com.zutubi.validation.i18n.DefaultTextProvider;
import com.zutubi.validation.i18n.TextProvider;

import java.util.*;

/**
 * Simple testing implementation of {@link com.zutubi.validation.ValidationContext}.
 */
public class FakeValidationContext implements ValidationContext
{
    private boolean ignoreAllFields = false;
    private Set<String> ignoredFields = new HashSet<String>();
    private List<String> actionErrors = new LinkedList<String>();
    private Map<String, List<String>> fieldErrors = new HashMap<String, List<String>>();
    private Config config = new PropertiesConfig();

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
        if (!ignoreAllFields && !ignoredFields.contains(field))
        {
            getFieldErrors(field).add(error);
        }
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

    public String getProperty(String key)
    {
        return config.getProperty(key);
    }

    public void setProperty(String key, String value)
    {
        config.setProperty(key, value);
    }

    public boolean hasProperty(String key)
    {
        return config.hasProperty(key);
    }

    public void removeProperty(String key)
    {
        config.removeProperty(key);
    }

    public boolean isWritable()
    {
        return config.isWritable();
    }
}
