package com.zutubi.pulse.tove.config.project.hooks;

import com.zutubi.config.annotations.Internal;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.events.build.BuildEvent;

/**
 * A build hook that is triggered automatically at some point in a build.
 */
@SymbolicName("zutubi.autoBuildHookConfig")
public abstract class AutoBuildHookConfiguration extends BuildHookConfiguration
{
    @Internal
    private boolean enabled = true;
    private boolean failOnError = false;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public boolean isFailOnError()
    {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError)
    {
        this.failOnError = failOnError;
    }

    public abstract boolean triggeredBy(BuildEvent event);

    public boolean failOnError()
    {
        return isFailOnError();
    }

    public boolean enabled()
    {
        return isEnabled();
    }
}
