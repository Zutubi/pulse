package com.zutubi.pulse.master.rest.model;

/**
 * Model for requesting available options for a property of a composite. Note that the composite
 * may not yet exist, in which case the baseName will not be set.
 */
public class OptionsModel
{
    private String baseName;
    private String symbolicName;
    private String propertyName;

    public String getBaseName()
    {
        return baseName;
    }

    public void setBaseName(String baseName)
    {
        this.baseName = baseName;
    }

    public String getSymbolicName()
    {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName)
    {
        this.symbolicName = symbolicName;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }
}
