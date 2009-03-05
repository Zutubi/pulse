package com.zutubi.pulse.core.marshal.doc;

/**
 * Stores documentation for a single attribute, including whether it is
 * required and any default value.
 */
public class AttributeDocs
{
    private String name;
    private String description;
    private boolean required;
    private String defaultValue;

    public AttributeDocs(String name, String description, boolean required, String defaultValue)
    {
        this.name = name;
        this.description = description;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean isRequired()
    {
        return required;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }
}
