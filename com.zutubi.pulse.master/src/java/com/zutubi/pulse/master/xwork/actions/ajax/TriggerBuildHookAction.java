package com.zutubi.pulse.master.xwork.actions.ajax;

import com.google.common.base.Predicate;
import static com.google.common.collect.Iterables.find;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookManager;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.pulse.master.xwork.actions.project.BuildActionBase;

/**
 * An action for manually triggering a build hook on a build.
 */
public class TriggerBuildHookAction extends BuildActionBase
{
    private long hook;
    private BuildHookManager buildHookManager;
    private SimpleResult result;

    public SimpleResult getResult()
    {
        return result;
    }

    public void setHook(long hook)
    {
        this.hook = hook;
    }

    public String execute() throws Exception
    {
        try
        {
            BuildResult buildResult = getRequiredBuildResult();
            Project project = buildResult.getProject();
            BuildHookConfiguration hookConfig = find(project.getConfig().getBuildHooks().values(), new Predicate<BuildHookConfiguration>()
            {
                public boolean apply(BuildHookConfiguration buildHookConfiguration)
                {
                    return buildHookConfiguration.getHandle() == hook;
                }
            }, null);

            if (hookConfig == null)
            {
                throw new LookupErrorException("Invalid hook handle " + hook);
            }

            buildHookManager.manualTrigger(hookConfig, buildResult);
            result = new SimpleResult(true, "triggered hook '" + hookConfig.getName() + "'");
        }
        catch (Exception e)
        {
            result = new SimpleResult(false, e.getMessage());
        }

        return SUCCESS;
    }

    public void setBuildHookManager(BuildHookManager buildHookManager)
    {
        this.buildHookManager = buildHookManager;
    }
}
