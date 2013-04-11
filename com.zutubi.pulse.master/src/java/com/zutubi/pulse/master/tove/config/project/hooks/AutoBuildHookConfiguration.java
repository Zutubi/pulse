package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.tove.annotations.Internal;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A build hook that is triggered automatically at some point in a build.
 */
@SymbolicName("zutubi.autoBuildHookConfig")
public abstract class AutoBuildHookConfiguration extends AbstractBuildHookConfiguration
{
    @Internal
    private boolean enabled = true;
    private boolean runForPersonal = false;
    private boolean allowManualTrigger = true;
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

    public boolean isRunForPersonal()
    {
        return runForPersonal;
    }

    public void setRunForPersonal(boolean runForPersonal)
    {
        this.runForPersonal = runForPersonal;
    }

    public boolean isAllowManualTrigger()
    {
        return allowManualTrigger;
    }

    public void setAllowManualTrigger(boolean allowManualTrigger)
    {
        this.allowManualTrigger = allowManualTrigger;
    }

    public boolean failOnError()
    {
        return isFailOnError();
    }

    public boolean enabled()
    {
        return isEnabled();
    }

    @Override
    public boolean canManuallyTriggerFor(BuildResult result)
    {
        return allowManualTrigger && (result == null || !result.isPersonal() || runForPersonal);
    }

    protected boolean triggeredByBuildType(BuildResult buildResult)
    {
        return !buildResult.isPersonal() || isRunForPersonal();
    }
}
