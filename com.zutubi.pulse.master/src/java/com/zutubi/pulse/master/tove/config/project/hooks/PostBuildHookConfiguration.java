package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.events.build.BuildEvent;
import com.zutubi.pulse.master.events.build.PostBuildEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Select;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedList;
import java.util.List;

/**
 * A post build hookis run when a build completes.
 */
@SymbolicName("zutubi.postBuildHookConfig")
@Form(fieldOrder = {"name", "runForAll", "runForStates", "failOnError", "runForPersonal", "allowManualTrigger"})
public class PostBuildHookConfiguration extends AutoBuildHookConfiguration
{
    @ControllingCheckbox(dependentFields = "runForStates", invert = true)
    private boolean runForAll = true;
    @Select(optionProvider = "com.zutubi.pulse.master.tove.CompletedResultStateOptionProvider")
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
        if(event instanceof PostBuildEvent)
        {
            BuildResult buildResult = event.getBuildResult();
            return triggeredByBuildType(buildResult) && stateMatches(buildResult);
        }

        return false;
    }

    private boolean stateMatches(BuildResult buildResult)
    {
        return runForAll || runForStates.contains(buildResult.getState());
    }

    public boolean appliesTo(BuildResult result)
    {
        return true;
    }

    public boolean appliesTo(RecipeResultNode result)
    {
        return false;
    }
}
