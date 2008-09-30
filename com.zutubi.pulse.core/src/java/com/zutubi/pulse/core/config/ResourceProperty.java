package com.zutubi.pulse.core.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Table;

/**
 */
@Form(fieldOrder = { "name", "value", "addToEnvironment", "addToPath", "resolveVariables" })
@Table(columns = {"name", "value"})
@SymbolicName("zutubi.resourceProperty")
public class ResourceProperty extends AbstractNamedConfiguration
{
    private String value;
    private boolean addToEnvironment = false;
    private boolean addToPath = false;
    private boolean resolveVariables = false;

    public ResourceProperty()
    {
    }

    public ResourceProperty(String name, String value)
    {
        this(name, value, false, false, false);
    }

    public ResourceProperty(String name, String value, boolean addToEnvironment, boolean addToPath, boolean resolveVariables)
    {
        super(name);
        this.value = value;
        this.addToEnvironment = addToEnvironment;
        this.addToPath = addToPath;
        this.resolveVariables = resolveVariables;
    }

    public ResourceProperty copy()
    {
        return new ResourceProperty(getName(), value, addToEnvironment, addToPath, resolveVariables);
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
}
