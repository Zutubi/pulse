package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.master.events.build.BuildEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

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
    public abstract boolean appliesTo(BuildResult result);
    public abstract boolean appliesTo(RecipeResultNode result);
    public abstract boolean failOnError();
    public abstract boolean enabled();
    /**
     * Indicates if this hook can be manually triggered for a given build
     * result.  Note that the result may be null, in which case this method
     * should just indicate if there are builds which this hook can be manually
     * triggered for.
     *
     * @param result the result to check triggerability for - may be null if
     *        the result has not yet been chosen by the user
     * @return true if this hook can be manually triggered for the given build
     *         result, or at all if the result is null
     */
    public abstract boolean canTriggerFor(BuildResult result);
}
