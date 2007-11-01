package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.pulse.events.build.BuildEvent;
import com.zutubi.config.annotations.SymbolicName;

/**
 * A build hook that is only ever triggered manually.
 */
@SymbolicName("zutubi.manualBuildHookConfig")
public class ManualBuildHookConfiguration extends BuildHookConfiguration
{
    public boolean triggeredBy(BuildEvent event)
    {
        return false;
    }
}
