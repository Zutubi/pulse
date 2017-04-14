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

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.LookupErrorException;

/**
 * Abstract base for actions that manipulate project responsibility.
 */
public abstract class ResponsibilityActionBase extends ActionSupport
{
    private long projectId;
    private SimpleResult result;

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    protected Project getProject()
    {
        Project project = projectManager.getProject(projectId, true);
        if (project == null)
        {
            throw new LookupErrorException("Unknown project [" + projectId + "]");
        }

        return project;
    }

    @Override
    public String execute() throws Exception
    {
        try
        {
            result = doExecute();
        }
        catch (Exception e)
        {
            result = new SimpleResult(false, e.getMessage());
        }

        return SUCCESS;
    }

    protected abstract SimpleResult doExecute();
}
