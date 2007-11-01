package com.zutubi.pulse.prototype.config.project.hooks;

import com.zutubi.pulse.events.build.BuildEvent;
import com.zutubi.pulse.events.build.BuildCompletedEvent;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.config.annotations.ControllingCheckbox;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Select;

import java.util.List;
import java.util.LinkedList;

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
        if(event instanceof BuildCompletedEvent)
        {
            BuildCompletedEvent bce = (BuildCompletedEvent) event;
            return runForAll || runForStates.contains(bce.getBuildResult().getState());
        }

        return false;
    }
}
