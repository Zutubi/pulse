package com.zutubi.prototype.config;

import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.validation.ValidationContext;
import com.zutubi.validation.i18n.TextProvider;

import java.util.*;

/**
 * A specialised validation context that carries extra information specific
 * to the Pulse configuration system.
 */
public class ConfigurationValidationContext implements ValidationContext
{
    private Configuration instance;
    private TextProvider textProvider;
    private Object parentInstance;
    private String baseName;
    private boolean ignoreRequired;
    private boolean ignoreAllFields = false;
    private Set<String> ignoredFields = new HashSet<String>();

    public ConfigurationValidationContext(Configuration instance, TextProvider textProvider, Object parentInstance, String baseName, boolean ignoreRequired)
    {
        this.instance = instance;
        this.textProvider = textProvider;
        this.parentInstance = parentInstance;
        this.baseName = baseName;
        this.ignoreRequired = ignoreRequired;
    }

    /**
     * @return the parent object of the object being validated.
     */
    public Object getParentInstance()
    {
        return parentInstance;
    }

    /**
     * @return the base name of the path of the object being validated, which
     *         may be null if the object is new 
     */
    public String getBaseName()
    {
        return baseName;
    }

    /**
     * @return true if the required validator should be ignored (as is the
     *         case when validating a template)
     */
    public boolean isIgnoreRequired()
    {
        return ignoreRequired;
    }

    public void addIgnoredField(String field)
    {
        ignoredFields.add(field);
        instance.clearFieldErrors(field);
    }

    public void addIgnoredFields(Set<String> fields)
    {
        for(String field: fields)
        {
            addIgnoredField(field);
        }
    }

    public void ignoreAllFields()
    {
        ignoreAllFields = true;
        instance.clearFieldErrors();
    }

    public void addActionError(String error)
    {
        instance.addInstanceError(error);
    }

    public void addFieldError(String field, String error)
    {
        if(!ignoreAllFields && !ignoredFields.contains(field))
        {
            instance.addFieldError(field, error);
        }
    }

    public Collection<String> getActionErrors()
    {
        return instance.getInstanceErrors();
    }

    public List<String> getFieldErrors(String field)
    {
        return instance.getFieldErrors(field);
    }

    public Map<String, List<String>> getFieldErrors()
    {
        return instance.getFieldErrors();
    }

    public boolean hasErrors()
    {
        return !instance.isValid();
    }

    public boolean hasFieldErrors()
    {
        return getFieldErrors().size() > 0;
    }

    public boolean hasFieldError(String field)
    {
        return getFieldErrors(field).size() > 0;
    }

    public boolean hasActionErrors()
    {
        return getActionErrors().size() > 0;
    }

    public void clearFieldErrors()
    {
        instance.clearFieldErrors();
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

    public TextProvider getTextProvider(Object context)
    {
        return textProvider.getTextProvider(context);
    }
}
