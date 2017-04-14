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
import com.zutubi.pulse.master.events.build.PreStageEvent;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A pre-stage hook is executed just after a build stage is assigned to an agent
 * - before it has been dispatched.  The hook can be easily applied to multiple
 * stages.
 */
@SymbolicName("zutubi.preStageHookConfig")
@Form(fieldOrder = {"name", "applyToAllStages", "stages", "failOnError", "runTaskOnAgents", "runForPersonal", "allowManualTrigger"})
public class PreStageHookConfiguration extends AbstractStageHookConfiguration
{
    public boolean triggeredBy(BuildEvent event)
    {
        if (event instanceof PreStageEvent)
        {
            PreStageEvent pse = (PreStageEvent) event;
            long stage = pse.getStageNode().getStageHandle();
            return triggeredByBuildType(pse.getBuildResult()) && stageMatches(stage);
        }

        return false;
    }
}
