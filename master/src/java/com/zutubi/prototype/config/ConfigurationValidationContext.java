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

    public ConfigurationValidationContext(ValidationAware validationAware, TextProvider textProvider, Object parentInstance, String baseName)
    {
        super(validationAware, textProvider);
        this.parentInstance = parentInstance;
        this.baseName = baseName;
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
}
