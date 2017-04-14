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
 * An event raised when a build is about to commence.  Raised just before the revision is finalised and dependencies are
 * resolved for the build.
 */
public class BuildCommencingEvent extends BuildEvent
{
    public BuildCommencingEvent(Object source, BuildResult buildResult, PulseExecutionContext context)
    {
        super(source, buildResult, context);
    }

    @Override
    public String toString()
    {
        return "Build Commencing Event: " + getBuildResult();
    }
}
