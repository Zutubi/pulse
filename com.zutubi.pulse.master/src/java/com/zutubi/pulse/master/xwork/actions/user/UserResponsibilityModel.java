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

import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.WebUtils;

/**
 * JSON-encodeable information about a project a user is responsible for.
 */
public class UserResponsibilityModel
{
    private String project;
    private long projectId;

    public UserResponsibilityModel(String project, long projectId)
    {
        this.project = project;
        this.projectId = projectId;
    }

    public String getId()
    {
        return getResponsibilityId(project);
    }

    public static String getResponsibilityId(String project)
    {
        return WebUtils.toValidHtmlName("responsibility-" + project);
    }

    public String getProject()
    {
        return project;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public String getUrl()
    {
        return Urls.getBaselessInstance().project(WebUtils.uriComponentEncode(project));
    }
}
