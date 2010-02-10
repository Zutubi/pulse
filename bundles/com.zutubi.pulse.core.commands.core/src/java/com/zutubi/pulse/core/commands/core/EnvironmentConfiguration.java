package com.zutubi.pulse.core.commands.core;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Configures an environment variable binding for a subprocess.
 */
@SymbolicName("zutubi.executableCommandConfig.environmentConfig")
@Form(fieldOrder = {"name", "value"})
@Table(columns = {"name", "value"})
public class EnvironmentConfiguration extends AbstractConfiguration
{
    @Required
    private String name;
    private String value;

    public EnvironmentConfiguration()
    {
    }

    public EnvironmentConfiguration(String name, String value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
