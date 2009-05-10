package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookManager;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;
import com.zutubi.pulse.master.xwork.actions.project.BuildActionBase;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

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
            BuildHookConfiguration hookConfig = CollectionUtils.find(project.getConfig().getBuildHooks().values(), new Predicate<BuildHookConfiguration>()
            {
                public boolean satisfied(BuildHookConfiguration buildHookConfiguration)
                {
                    return buildHookConfiguration.getHandle() == hook;
                }
            });

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
