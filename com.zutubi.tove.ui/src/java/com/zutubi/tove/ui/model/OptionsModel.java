package com.zutubi.tove.ui.model;

/**
 * Model for requesting available options for a property of a composite. Note that the composite
 * may not yet exist, in which case the baseName will not be set.
 */
public class OptionsModel
{
    private String baseName;
    private String symbolicName;
    private String propertyName;
    private String scopePath;

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

    /**
     * @return path of the scope instance to search for options from: used for looking up referenceable instances when
     *         a dependentOn field is specified in the {@link @Reference} (see
     *         {@link com.zutubi.tove.ui.forms.ReferenceAnnotationHandler} and the trackdependent script).
     */
    public String getScopePath()
    {
        return scopePath;
    }

    public void setScopePath(String scopePath)
    {
        this.scopePath = scopePath;
    }
}
