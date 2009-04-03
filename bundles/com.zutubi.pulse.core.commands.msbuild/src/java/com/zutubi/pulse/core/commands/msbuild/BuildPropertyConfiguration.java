package com.zutubi.pulse.core.commands.msbuild;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Configures a named build property to pass to MsBuild.
 */
@SymbolicName("zutubi.msbuildCommandConfig.buildPropertyConfig")
@Form(fieldOrder = {"name", "value"})
public class BuildPropertyConfiguration extends AbstractNamedConfiguration
{
    @Required
    private String value;

    public BuildPropertyConfiguration()
    {
    }

    public BuildPropertyConfiguration(String name, String value)
    {
        setName(name);
        setValue(value);
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
