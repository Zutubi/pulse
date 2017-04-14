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

package com.zutubi.pulse.master.build.queue;

import com.zutubi.pulse.master.build.control.BuildController;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;

/**
 * Holds an activated build request.
 *
 * A build request is activated when it is considered ready to build.
 * The build then receives a build controller and awaits dispatch to
 * the first available agent for building.
 *
 * A build remains activated from the moment is leaves the queue to the
 * time the scheduler receives a build completed event.
 */
public class ActivatedRequest extends RequestHolder
{
    private BuildController controller;
    private boolean buildCommenced;

    protected ActivatedRequest(BuildRequestEvent request)
    {
        super(request);
    }

    public void setController(BuildController controller)
    {
        this.controller = controller;
    }

    public BuildController getController()
    {
        return controller;
    }

    /**
     * @return true if the build associated with this request has commenced
     */
    public boolean isBuildCommenced()
    {
        return buildCommenced;
    }

    /**
     * Marks this request as corresponding to a build that has commenced.
     */
    public void buildCommenced()
    {
        buildCommenced = true;
    }
}
