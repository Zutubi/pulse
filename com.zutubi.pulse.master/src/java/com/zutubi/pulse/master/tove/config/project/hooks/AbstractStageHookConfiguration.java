package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Reference;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedList;
import java.util.List;

/**
 * Shared base class for stage-based hooks.
 */
@SymbolicName("zutubi.abstractStageHookConfig")
public abstract class AbstractStageHookConfiguration extends AutoBuildHookConfiguration
{
    @ControllingCheckbox(uncheckedFields = "stages")
    private boolean applyToAllStages = true;
    @Reference
    private List<BuildStageConfiguration> stages = new LinkedList<BuildStageConfiguration>();
    private boolean runTaskOnAgents;

    public boolean isApplyToAllStages()
    {
        return applyToAllStages;
    }

    public void setApplyToAllStages(boolean applyToAllStages)
    {
        this.applyToAllStages = applyToAllStages;
    }

    public List<BuildStageConfiguration> getStages()
    {
        return stages;
    }

    public void setStages(List<BuildStageConfiguration> stages)
    {
        this.stages = stages;
    }

    public boolean isRunTaskOnAgents()
    {
        return runTaskOnAgents;
    }

    public void setRunTaskOnAgents(boolean runTaskOnAgents)
    {
        this.runTaskOnAgents = runTaskOnAgents;
    }

    public boolean appliesTo(BuildResult result)
    {
        return false;
    }

    public boolean appliesTo(RecipeResultNode result)
    {
        return stageMatches(result.getStageHandle());
    }

    public boolean runsOnAgent()
    {
        return runTaskOnAgents;
    }

    protected boolean stageMatches(long stage)
    {
        if (stage == 0)
        {
            return false;
        }

        if (applyToAllStages)
        {
            return true;
        }

        for (BuildStageConfiguration stageConfig: stages)
        {
            if (stageConfig.getHandle() == stage)
            {
                return true;
            }
        }

        return false;
    }
}
