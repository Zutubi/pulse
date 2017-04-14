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

import com.zutubi.pulse.master.model.BuildResult;

/**
 * Event with a custom status message for a build.
 */
public class BuildStatusEvent extends BuildEvent
{
    protected String message;

    public BuildStatusEvent(Object source, BuildResult buildResult, String message)
    {
        super(source, buildResult, null);
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public String toString()
    {
        return "Build Status Event " + getId() + ": " + message;
    }
}
