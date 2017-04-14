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
import com.zutubi.pulse.core.model.RecipeResult;
import com.zutubi.pulse.master.events.build.BuildEvent;
import com.zutubi.pulse.master.events.build.PostStageEvent;
import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.ItemPicker;
import com.zutubi.tove.annotations.SymbolicName;

import java.util.LinkedList;
import java.util.List;

/**
 * A post-stage hook is executed just after a build stage is completed.  The
 * hook can be easily applied to multiple stages.
 */
@SymbolicName("zutubi.postStageHookConfig")
@Form(fieldOrder = {"name", "applyToAllStages", "stages", "runForAll", "runForStates", "failOnError", "runTaskOnAgents", "runForPersonal", "allowManualTrigger"})
public class PostStageHookConfiguration extends AbstractStageHookConfiguration
{
    @ControllingCheckbox(uncheckedFields = "runForStates")
    private boolean runForAll = true;
    @ItemPicker(optionProvider = "com.zutubi.pulse.master.tove.config.CompletedResultStateOptionProvider")
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
