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

import com.zutubi.events.Event;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.model.Result;
import com.zutubi.pulse.master.model.BuildResult;

/**
 */
public class BuildEvent extends Event
{
    private BuildResult buildResult;
    private PulseExecutionContext context;

    public BuildEvent(Object source, BuildResult buildResult, PulseExecutionContext context)
    {
        super(source);
        this.buildResult = buildResult;
        this.context = context;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public Result getResult()
    {
        return buildResult;
    }

    public long getMetaBuildId()
    {
        return buildResult.getMetaBuildId();
    }

    public PulseExecutionContext getContext()
    {
        return context;
    }

    public String toString()
    {
        return "Build Event";
    }
}
