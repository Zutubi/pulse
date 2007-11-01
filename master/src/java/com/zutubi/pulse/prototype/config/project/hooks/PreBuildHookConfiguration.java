package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.pulse.events.build.BuildEvent;
import com.zutubi.pulse.events.build.PreBuildEvent;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Form;

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
}
