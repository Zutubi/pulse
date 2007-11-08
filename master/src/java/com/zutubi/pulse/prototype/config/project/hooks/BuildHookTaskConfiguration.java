package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.config.Configuration;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;

/**
 * A build hook task is the action performed when a build hook is triggered.
 */
@SymbolicName("zutubi.buildHookTask")
public interface BuildHookTaskConfiguration extends Configuration
{
    void execute(ExecutionContext context, BuildResult buildResult, RecipeResultNode resultNode) throws Exception;
}
