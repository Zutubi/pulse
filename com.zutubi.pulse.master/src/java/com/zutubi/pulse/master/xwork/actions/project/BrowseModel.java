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

import flexjson.JSON;

import java.util.List;

/**
 * Holder for JSON data sent to the browse view.
 */
public class BrowseModel
{
    private String projectsFilter;
    private List<ProjectsModel> projectGroups;
    private List<String> invalidProjects;

    public String getProjectsFilter()
    {
        return projectsFilter;
    }

    public void setProjectsFilter(String projectsFilter)
    {
        this.projectsFilter = projectsFilter;
    }

    @JSON
    public List<ProjectsModel> getProjectGroups()
    {
        return projectGroups;
    }

    public void setProjectGroups(List<ProjectsModel> projectGroups)
    {
        this.projectGroups = projectGroups;
    }

    @JSON
    public List<String> getInvalidProjects()
    {
        return invalidProjects;
    }

    public void setInvalidProjects(List<String> invalidProjects)
    {
        this.invalidProjects = invalidProjects;
    }
}
