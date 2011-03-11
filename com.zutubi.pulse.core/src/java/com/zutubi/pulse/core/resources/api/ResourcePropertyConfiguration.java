package com.zutubi.pulse.core.resources.api;

import com.zutubi.pulse.core.engine.api.ResourceProperty;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

@Form(fieldOrder = { "name", "value", "description", "addToEnvironment", "addToPath", "resolveVariables" })
@Table(columns = {"name", "value"})
@SymbolicName("zutubi.resourceProperty")
public class ResourcePropertyConfiguration extends AbstractNamedConfiguration
{
    private String value;
    private boolean addToEnvironment = false;
    private boolean addToPath = false;
    private boolean resolveVariables = false;
    private String description;

    public ResourcePropertyConfiguration()
    {
    }

    public ResourcePropertyConfiguration(String name, String value)
    {
        this(name, value, false, false, false);
    }

    public ResourcePropertyConfiguration(String name, String value, boolean addToEnvironment, boolean addToPath, boolean resolveVariables)
    {
        super(name);
        this.value = value;
        this.addToEnvironment = addToEnvironment;
        this.addToPath = addToPath;
        this.resolveVariables = resolveVariables;
    }

    public ResourcePropertyConfiguration(ResourceProperty p)
    {
        this(p.getName(), p.getValue(), p.getAddToEnvironment(), p.getAddToPath(), p.getResolveVariables());
    }

    public ResourcePropertyConfiguration copy()
    {
        return new ResourcePropertyConfiguration(getName(), value, addToEnvironment, addToPath, resolveVariables);
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public boolean getAddToEnvironment()
    {
        return addToEnvironment;
    }

    public void setAddToEnvironment(boolean addToEnvironment)
    {
        this.addToEnvironment = addToEnvironment;
    }

    public boolean getAddToPath()
    {
        return addToPath;
    }

    public void setAddToPath(boolean addToPath)
    {
        this.addToPath = addToPath;
    }

    public boolean getResolveVariables()
    {
        return resolveVariables;
    }

    public void setResolveVariables(boolean resolveVariables)
    {
        this.resolveVariables = resolveVariables;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public ResourceProperty asResourceProperty()
    {
        return new ResourceProperty(getName(), getValue(), getAddToEnvironment(), getAddToPath(), getResolveVariables());
    }
}