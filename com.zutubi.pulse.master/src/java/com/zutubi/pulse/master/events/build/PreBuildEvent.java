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

/**
 * This event is raised by the build controller just before it commences a
 * build.  Handle this event for tasks that should be run before the build
 * controller actually begins the build process.
 */
public class PreBuildEvent extends BuildEvent
{
    public PreBuildEvent(Object source, BuildResult result, PulseExecutionContext context)
    {
        super(source, result, context);
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder("Pre Build Event");
        if (getBuildResult() != null)
        {
            builder.append(": ").append(getBuildResult().getId());
        }
        return builder.toString();
    }
}
