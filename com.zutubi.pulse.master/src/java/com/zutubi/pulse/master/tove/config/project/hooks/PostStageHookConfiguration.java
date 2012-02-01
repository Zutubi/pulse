package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.events.build.BuildEvent;
import com.zutubi.pulse.master.events.build.PostStageEvent;
import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Select;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedList;
import java.util.List;

/**
 * A post-stage hook is executed just after a build stage is completed.  The
 * hook can be easily applied to multiple stages.
 */
@SymbolicName("zutubi.postStageHookConfig")
@Form(fieldOrder = {"name", "applyToAllStages", "stages", "runForAll", "runForStates", "failOnError", "runForPersonal", "allowManualTrigger"})
public class PostStageHookConfiguration extends AbstractStageHookConfiguration
{
    @ControllingCheckbox(uncheckedFields = "runForStates")
    private boolean runForAll = true;
    @Select(optionProvider = "com.zutubi.pulse.master.tove.config.CompletedResultStateOptionProvider")
    private List<ResultState> runForStates = new LinkedList<ResultState>();

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
            return triggeredByBuildType(pse.getBuildResult()) && stageMatches(stage) && stateMatches(pse);
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
