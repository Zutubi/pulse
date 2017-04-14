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

package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * An action for toggling a build's pinned status.
 * */
public class TogglePinAction extends ActionSupport
{
    private long buildId;
    private boolean pin;
    private SimpleResult result;

    private BuildManager buildManager;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public void setPin(boolean pin)
    {
        this.pin = pin;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    public String execute() throws Exception
    {
        BuildResult buildResult = buildManager.getBuildResult(buildId);
        if (buildResult == null)
        {
            result = new SimpleResult(false, "Unknown build [" + buildId + "]");
        }
        else
        {
            try
            {
                buildManager.togglePin(buildResult, pin);
                result = new SimpleResult(true, "Build " + (pin ? "pinned" : "unpinned") + ".");
            }
            catch (Exception e)
            {
                result = new SimpleResult(false, e.getMessage());
            }
        }

        return SUCCESS;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
