package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.tove.annotations.SymbolicName;

/**
 * Abstract base for hooks that apply to builds (as opposed to stages).
 */
@SymbolicName("zutubi.abstractBuildHookConfig")
public abstract class AbstractBuildHookConfiguration extends BuildHookConfiguration
{
    public boolean runsOnAgent()
    {
        return false;
    }
}
