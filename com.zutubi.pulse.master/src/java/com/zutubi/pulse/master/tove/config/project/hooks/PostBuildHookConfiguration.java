/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.core.engine.api.ResultState;
import com.zutubi.pulse.master.events.build.BuildEvent;
import com.zutubi.pulse.master.events.build.PostBuildEvent;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;
import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.ItemPicker;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedList;
import java.util.List;

/**
 * A post build hook is run when a build completes.
 */
@SymbolicName("zutubi.postBuildHookConfig")
@Form(fieldOrder = {"name", "runForAll", "runForStates", "failOnError", "runForPersonal", "runTaskOnAgents", "allowManualTrigger"})
public class PostBuildHookConfiguration extends AutoBuildHookConfiguration
{
    @ControllingCheckbox(uncheckedFields = "runForStates")
    private boolean runForAll = true;
    @ItemPicker(optionProvider = "com.zutubi.pulse.master.tove.config.CompletedResultStateOptionProvider")
    private List<ResultState> runForStates = new LinkedList<ResultState>();
    private boolean runTaskOnAgents;

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

    public boolean isRunTaskOnAgents()
    {
        return runTaskOnAgents;
    }

    public void setRunTaskOnAgents(boolean runTaskOnAgents)
    {
        this.runTaskOnAgents = runTaskOnAgents;
    }

    @Override
    public boolean runsOnAgent()
    {
        return runTaskOnAgents;
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
