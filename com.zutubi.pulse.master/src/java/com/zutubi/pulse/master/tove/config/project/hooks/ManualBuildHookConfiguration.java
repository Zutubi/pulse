package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.master.events.build.BuildEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A build hook that is only ever triggered manually.
 */
@SymbolicName("zutubi.manualBuildHookConfig")
@Form(fieldOrder = "name")
public class ManualBuildHookConfiguration extends BuildHookConfiguration
{
    public boolean triggeredBy(BuildEvent event)
    {
        return false;
    }

    public boolean appliesTo(BuildResult result)
    {
        return true;
    }

    public boolean appliesTo(RecipeResultNode result)
    {
        return false;
    }

    public boolean failOnError()
    {
        return false;
    }

    public boolean enabled()
    {
        return true;
    }

    @Override
    public boolean canTriggerFor(BuildResult result)
    {
        return true;
    }
}
