package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.master.events.build.BuildEvent;
import com.zutubi.pulse.master.events.build.PreBuildEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 */
@SymbolicName("zutubi.preBuildHookConfig")
@Form(fieldOrder = {"name", "failOnError", "runForPersonal", "allowManualTrigger"})
public class PreBuildHookConfiguration extends AutoBuildHookConfiguration
{
    public boolean triggeredBy(BuildEvent event)
    {
        if (event instanceof PreBuildEvent)
        {
            PreBuildEvent pbe = (PreBuildEvent) event;
            return !pbe.getBuildResult().isPersonal() || isRunForPersonal();
        }

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
}
