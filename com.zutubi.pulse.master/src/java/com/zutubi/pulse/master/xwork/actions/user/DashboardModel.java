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

import com.zutubi.pulse.master.xwork.actions.project.ProjectsModel;
import flexjson.JSON;

import java.util.List;

/**
 * Holder for all JSON data sent to the UI to render the dashboard.
 */
public class DashboardModel
{
    private List<String> contactPointsWithErrors;
    private List<UserResponsibilityModel> responsibilities;
    private String projectsFilter;
    private List<ProjectsModel> projects;
    private List<ChangelistModel> myChanges;
    private List<ChangelistModel> myProjectChanges;

    public DashboardModel(List<String> contactPointsWithErrors, List<UserResponsibilityModel> responsibilities, String projectsFilter, List<ProjectsModel> projects, List<ChangelistModel> myChanges, List<ChangelistModel> myProjectChanges)
    {
        this.contactPointsWithErrors = contactPointsWithErrors;
        this.responsibilities = responsibilities;
        this.projectsFilter = projectsFilter;
        this.projects = projects;
        this.myChanges = myChanges;
        this.myProjectChanges = myProjectChanges;
    }

    @JSON
    public List<String> getContactPointsWithErrors()
    {
        return contactPointsWithErrors;
    }

    @JSON
    public List<UserResponsibilityModel> getResponsibilities()
    {
        return responsibilities;
    }

    public String getProjectsFilter()
    {
        return projectsFilter;
    }

    @JSON
    public List<ProjectsModel> getProjects()
    {
        return projects;
    }

    @JSON
    public List<ChangelistModel> getMyChanges()
    {
        return myChanges;
    }

    @JSON
    public List<ChangelistModel> getMyProjectChanges()
    {
        return myProjectChanges;
    }
}
