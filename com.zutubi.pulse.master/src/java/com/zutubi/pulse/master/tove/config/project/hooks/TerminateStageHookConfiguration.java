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

import com.zutubi.pulse.master.events.build.BuildEvent;
import com.zutubi.pulse.master.events.build.TerminateStageEvent;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A terminate-stage hook is executed just after a request to terminate a
 * recipe/build stage is sent to an agent.
 */
@SymbolicName("zutubi.terminateStageHookConfig")
@Form(fieldOrder = {"name", "applyToAllStages", "stages", "failOnError", "runTaskOnAgents", "runForPersonal", "allowManualTrigger"})
public class TerminateStageHookConfiguration extends AbstractStageHookConfiguration
{
    public boolean triggeredBy(BuildEvent event)
    {
        if (event instanceof TerminateStageEvent)
        {
            TerminateStageEvent tse = (TerminateStageEvent) event;
            long stage = tse.getStageNode().getStageHandle();
            return triggeredByBuildType(tse.getBuildResult()) && stageMatches(stage);
        }

        return false;
    }
}
