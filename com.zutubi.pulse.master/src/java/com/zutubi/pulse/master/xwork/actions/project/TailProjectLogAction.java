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

package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.project.ProjectLogger;
import com.zutubi.pulse.master.project.ProjectLoggerManager;

import java.io.IOException;

public class TailProjectLogAction extends TailBuildLogAction
{
    private ProjectLoggerManager projectLoggerManager;

    public String execute() throws Exception
    {
        initialiseProperties();

        Project project = getProject();

        ProjectLogger logger = projectLoggerManager.getLogger(project.getId());

        this.logExists = true;

        if (raw)
        {
            try
            {
                inputStream = logger.openStream();
                return "raw";
            }
            catch (IOException e)
            {
                addActionError("Unable to open project log: " + e.getMessage());
                return ERROR;
            }
        }
        else
        {
            this.tail = logger.tail(maxLines);
            return "tail";
        }
    }

    public void setProjectLoggerManager(ProjectLoggerManager projectLoggerManager)
    {
        this.projectLoggerManager = projectLoggerManager;
    }
}
