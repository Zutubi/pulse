package com.zutubi.pulse.tove.config.project.hooks;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.master.events.build.BuildEvent;
import com.zutubi.pulse.master.events.build.PreBuildEvent;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;

/**
 */
@SymbolicName("zutubi.preBuildHookConfig")
@Form(fieldOrder = {"name", "failOnError"})
public class PreBuildHookConfiguration extends AutoBuildHookConfiguration
{
    public boolean triggeredBy(BuildEvent event)
    {
        return event instanceof PreBuildEvent;
    }

    public boolean appliesTo(BuildResult result)
    {
        return true;
    }

    public boolean appliesTo(RecipeResultNode result)
    {
        return false;
    }
}
