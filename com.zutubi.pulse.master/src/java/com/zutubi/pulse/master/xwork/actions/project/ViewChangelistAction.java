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

import com.zutubi.pulse.core.model.PersistentChangelist;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.util.StringUtils;

/**
 * Action to display information about a single changelist.  This action does
 * not fit cleanly into our hierarchy as it can be accessed from multiple
 * locations, even for a single changelist.
 */
public class ViewChangelistAction extends ActionSupport
{
    private long id;
    private PersistentChangelist changelist;
    private int startPage;
    /**
     * If we drilled down from the project, this is the project ID
     */
    private String projectName;
    private Project project;

    /**
     * This is the build result we have drilled down from, if any.
     */
    private String buildVID;
    private BuildResult buildResult;

    private Viewport viewport;

    private BuildManager buildManager;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public int getStartPage()
    {
        return startPage;
    }

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public void setProjectName(String projectName)
    {
        this.projectName = projectName;
    }

    public String getu_projectName()
    {
        return uriComponentEncode(projectName);
    }

    public String geth_projectName()
    {
        return htmlEncode(projectName);
    }

    public Project getProject()
    {
        return project;
    }

    public String getBuildVID()
    {
        return buildVID;
    }

    public void setBuildVID(String buildVID)
    {
        this.buildVID = buildVID;
    }

    public BuildResult getBuildResult()
    {
        return buildResult;
    }

    public PersistentChangelist getChangelist()
    {
        return changelist;
    }

    public BuildManager getBuildManager()
    {
        return buildManager;
    }

    public Viewport getViewport()
    {
        if (buildResult != null && viewport == null)
        {
            viewport = loadBuildNavViewport(buildResult.getId());
        }
        return viewport;
    }

    public String execute()
    {
        changelist = changelistManager.getChangelist(id);
        if (changelist == null)
        {
            addActionError("Unknown changelist '" + id + "'");
            return ERROR;
        }

        if (StringUtils.stringSet(projectName))
        {
            project = projectManager.getProject(projectName, false);
        }
        if (StringUtils.stringSet(buildVID))
        {
            // It is valid to have no build ID set: we may not be viewing
            // the change as part of a build.
            buildResult = buildManager.getByProjectAndVirtualId(project, buildVID);
        }

        return SUCCESS;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
