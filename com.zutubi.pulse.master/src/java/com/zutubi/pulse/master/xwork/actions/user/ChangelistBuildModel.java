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

package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.util.WebUtils;

/**
 * JSON-encodable object representing a build affected by a changelist.
 */
public class ChangelistBuildModel
{
    private BuildResult buildResult;

    public ChangelistBuildModel(BuildResult buildResult)
    {
        this.buildResult = buildResult;
    }

    public String getProject()
    {
        return buildResult.getProject().getName();
    }

    public String getEncodedProject()
    {
        return WebUtils.uriComponentEncode(getProject());
    }

    public long getNumber()
    {
        return buildResult.getNumber();
    }

    public String getStatus()
    {
        return buildResult.getState().getPrettyString();
    }

    public String getStatusIcon()
    {
        return ToveUtils.getStatusIcon(buildResult);
    }

    public String getStatusClass()
    {
        return ToveUtils.getStatusClass(buildResult);
    }
}
