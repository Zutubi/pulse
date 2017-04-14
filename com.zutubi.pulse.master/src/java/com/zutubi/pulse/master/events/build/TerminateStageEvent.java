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

package com.zutubi.pulse.master.events.build;

import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.RecipeResultNode;

/**
 * An event raised when a request has been made to terminate a stage (either via a timeout or
 * explicit user request).
 */
public class TerminateStageEvent extends StageEvent
{
    public TerminateStageEvent(Object source, BuildResult result, RecipeResultNode stageNode, PulseExecutionContext context)
    {
        super(source, result, stageNode, context);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Terminate Stage Event");
        if (getBuildResult() != null)
        {
            builder.append(": ").append(getBuildResult().getId());
        }
        if (getStageNode() != null)
        {
            builder.append(": ").append(getStageNode().getStageName());
        }
        return builder.toString();
    }
}
