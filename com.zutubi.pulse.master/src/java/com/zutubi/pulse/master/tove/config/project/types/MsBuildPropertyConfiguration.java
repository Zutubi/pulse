package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * Simlpe build properties for MsBuild projects.  Allows easy configuration of
 * extra properties to define for a build, as this is quite a common task.
 */
@SymbolicName("zutubi.msbuildPropertyConfig")
@Form(fieldOrder = {"name", "value"})
public class MsBuildPropertyConfiguration extends AbstractNamedConfiguration
{
    @Required
    private String value;

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
