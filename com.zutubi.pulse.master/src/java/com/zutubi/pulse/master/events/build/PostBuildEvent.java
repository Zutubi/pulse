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
 * This event is raised by the build processor when a build is complete, but
 * before the final indexing and saving of the build result.  Handle this
 * event for tasks that should happen after a build but that may wish to
 * modify the build result.
 */
public class PostBuildEvent extends BuildEvent
{
    public PostBuildEvent(Object source, BuildResult result, PulseExecutionContext context)
    {
        super(source, result, context);
    }

    public String toString()
    {
        return "Post Build Event: " + getBuildResult();
    }
}
