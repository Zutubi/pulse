package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.events.build.BuildEvent;
import com.zutubi.pulse.master.events.build.PostStageEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.pulse.master.tove.config.project.BuildStageConfiguration;
import com.zutubi.tove.annotations.*;

import java.util.LinkedList;
import java.util.List;

/**
 * A post-stage hook is executed just after a build stage is completed.  The
 * hook can be easily applied to multiple stages.
 */
@SymbolicName("zutubi.postStageHookConfig")
@Form(fieldOrder = {"name", "applyToAllStages", "stages", "runForAll", "runForStates", "failOnError", "runForPersonal", "allowManualTrigger"})
public class PostStageHookConfiguration extends AutoBuildHookConfiguration
{
    @ControllingCheckbox(dependentFields = "stages", invert = true)
    private boolean applyToAllStages = true;
    @Reference
    private List<BuildStageConfiguration> stages = new LinkedList<BuildStageConfiguration>();
    @ControllingCheckbox(dependentFields = "runForStates", invert = true)
    private boolean runForAll = true;
    @Select(optionProvider = "com.zutubi.pulse.master.tove.CompletedResultStateOptionProvider")
    private List<ResultState> runForStates = new LinkedList<ResultState>();

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

    public boolean isRunForAll()
    {
        return runForAll;
    }

    public void setRunForAll(boolean runForAll)
    {
        this.runForAll = runForAll;
    }

    public List<ResultState> getRunForStates()
    {
        return runForStates;
    }

    public void setRunForStates(List<ResultState> runForStates)
    {
        this.runForStates = runForStates;
    }

    public boolean triggeredBy(BuildEvent event)
    {
        if(event instanceof PostStageEvent)
        {
            PostStageEvent pse = (PostStageEvent) event;
            long stage = pse.getStageNode().getStageHandle();
            return (!pse.getBuildResult().isPersonal() || isRunForPersonal()) && stageMatches(stage) && stateMatches(pse);
        }

        return false;
    }

    public boolean appliesTo(BuildResult result)
    {
        return false;
    }

    public boolean appliesTo(RecipeResultNode result)
    {
        return stageMatches(result.getStageHandle());
    }

    private boolean stageMatches(long stage)
    {
        if(stage == 0)
        {
            return false;
        }
        
        if(applyToAllStages)
        {
            return true;
        }
        
        for(BuildStageConfiguration stageConfig: stages)
        {
            if(stageConfig.getHandle() == stage)
            {
                return true;
            }
        }
        return false;
    }

    private boolean stateMatches(PostStageEvent pse)
    {
        RecipeResult result = pse.getStageNode().getResult();
        return stateMatches(result);
    }

    private boolean stateMatches(RecipeResult result)
    {
        return runForAll || runForStates.contains(result.getState());
    }
}
