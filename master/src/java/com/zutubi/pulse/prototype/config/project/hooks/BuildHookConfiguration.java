package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.events.build.BuildEvent;

/**
 * A build hook is a task that runs on the Pulse master at some point during
 * a build.  Hooks can be triggered at times such as pre/post build, pre/post
 * stage or even manually.
 */
@SymbolicName("zutubi.buildHookConfig")
public abstract class BuildHookConfiguration extends AbstractNamedConfiguration
{
    private BuildHookTaskConfiguration task;

    public BuildHookTaskConfiguration getTask()
    {
        return task;
    }

    public void setTask(BuildHookTaskConfiguration task)
    {
        this.task = task;
    }

    public abstract boolean triggeredBy(BuildEvent event);
}
