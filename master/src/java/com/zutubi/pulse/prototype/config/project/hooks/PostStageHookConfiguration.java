package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.events.build.BuildEvent;
import com.zutubi.pulse.events.build.PreBuildEvent;
import com.zutubi.pulse.events.build.RecipeCommencedEvent;
import com.zutubi.pulse.events.build.PostStageEvent;
import com.zutubi.pulse.prototype.config.project.BuildStageConfiguration;
import com.zutubi.pulse.core.model.ResultState;

import java.util.List;
import java.util.LinkedList;

/**
 * A post-stage hook is executed just after a build stage is completed.  The
 * hook can be easily applied to multiple stages.
 */
@SymbolicName("zutubi.preStageHookConfig")
@Form(fieldOrder = {"name", "applyToAllStages", "stages", "runForAll", "runForStates", "failOnError"})
public class PostStageHookConfiguration extends AutoBuildHookConfiguration
{
    @ControllingCheckbox(dependentFields = "stages", invert = true)
    private boolean applyToAllStages = true;
    @Reference
    private List<BuildStageConfiguration> stages = new LinkedList<BuildStageConfiguration>();
    @ControllingCheckbox(dependentFields = "runForStates", invert = true)
    private boolean runForAll = true;
    @Select(optionProvider = "com.zutubi.pulse.prototype.CompletedResultStateOptionProvider")
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
            if(applyToAllStages)
            {
                return stateMatches(pse);
            }
            else
            {
                long stage = pse.getStageNode().getStageHandle();
                for(BuildStageConfiguration stageConfig: stages)
                {
                    if(stageConfig.getHandle() == stage)
                    {
                        return stateMatches(pse);
                    }
                }
            }
        }

        return false;
    }

    private boolean stateMatches(PostStageEvent pse)
    {
        return runForAll || runForStates.contains(pse.getStageNode().getResult().getState());
    }
}
