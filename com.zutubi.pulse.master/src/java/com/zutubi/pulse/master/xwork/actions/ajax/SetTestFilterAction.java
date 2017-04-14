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

import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.project.TestFilterContext;

/**
 * Simple ajax action to store a user's test filter preference for a build in
 * their session.
 */
public class SetTestFilterAction extends ActionSupport
{
    private long buildId;
    private String filter;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public void setFilter(String filter)
    {
        this.filter = filter;
    }

    public String execute()
    {
        TestFilterContext.setFilterForBuild(buildId, filter);
        return SUCCESS;
    }
}