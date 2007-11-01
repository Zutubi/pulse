package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.config.annotations.SymbolicName;

/**
 * A build hook task is the action performed when a build hook is triggered.
 */
@SymbolicName("zutubi.buildHookTask")
public interface BuildHookTaskConfiguration extends Configuration
{
    boolean execute(BuildHookContext context);
}
