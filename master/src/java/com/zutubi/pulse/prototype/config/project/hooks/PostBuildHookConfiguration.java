package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.config.annotations.ControllingCheckbox;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Select;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.events.build.BuildEvent;
import com.zutubi.pulse.events.build.PostBuildEvent;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.RecipeResultNode;

import java.util.LinkedList;
import java.util.List;

/**
 * A post build hookis run when a build completes.
 */
@SymbolicName("zutubi.postBuildHookConfig")
@Form(fieldOrder = {"name", "runForAll", "runForStates", "failOnError"})
public class PostBuildHookConfiguration extends AutoBuildHookConfiguration
{
    @ControllingCheckbox(dependentFields = "runForStates", invert = true)
    private boolean runForAll = true;
    @Select(optionProvider = "com.zutubi.pulse.prototype.CompletedResultStateOptionProvider")
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
            PostBuildEvent pbe = (PostBuildEvent) event;
            BuildResult buildResult = pbe.getBuildResult();
            return stateMatches(buildResult);
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
