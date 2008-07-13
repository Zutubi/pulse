package com.zutubi.pulse.tove.config.project;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.validation.annotations.Constraint;

/**
 * A transient configuration that can be used to select a project build.
 */
@SymbolicName("zutubi.buildSelectorConfig")
@Form(fieldOrder = "build")
public class BuildSelectorConfiguration extends AbstractConfiguration
{
    @Constraint("BuildValidator")
    private String build;

    public String getBuild()
    {
        return build;
    }

    public void setBuild(String build)
    {
        this.build = build;
    }
}
