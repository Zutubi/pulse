package com.zutubi.pulse.master.rest.model;

import java.util.Set;

/**
 * Model for validating configuration.
 */
public class ValidateModel
{
    private String baseName;
    private boolean concrete;
    private Set<String> ignoredFields;
    private CompositeModel composite;

    public String getBaseName()
    {
        return baseName;
    }

    public void setBaseName(String baseName)
    {
        this.baseName = baseName;
    }

    public boolean isConcrete()
    {
        return concrete;
    }

    public void setConcrete(boolean concrete)
    {
        this.concrete = concrete;
    }

    public Set<String> getIgnoredFields()
    {
        return ignoredFields;
    }

    public void setIgnoredFields(Set<String> ignoredFields)
    {
        this.ignoredFields = ignoredFields;
    }

    public CompositeModel getComposite()
    {
        return composite;
    }

    public void setComposite(CompositeModel composite)
    {
        this.composite = composite;
    }
}
