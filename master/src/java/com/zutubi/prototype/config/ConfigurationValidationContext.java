package com.zutubi.prototype.config;

import com.zutubi.validation.DelegatingValidationContext;
import com.zutubi.validation.ValidationAware;
import com.zutubi.validation.i18n.TextProvider;

/**
 * A specialised validation context that carries extra information specific
 * to the Pulse configuration system.
 */
public class ConfigurationValidationContext extends DelegatingValidationContext
{
    private Object parentInstance;
    private String baseName;
    private boolean ignoreRequired;

    public ConfigurationValidationContext(ValidationAware validationAware, TextProvider textProvider, Object parentInstance, String baseName, boolean ignoreRequired)
    {
        super(validationAware, textProvider);
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
}
