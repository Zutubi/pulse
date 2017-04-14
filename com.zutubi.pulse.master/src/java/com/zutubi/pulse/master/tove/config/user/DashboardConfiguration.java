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

package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.annotations.*;

import java.util.LinkedList;
import java.util.List;

/**
 * User preferences controlling what they see on their dashboard.
 */
@SymbolicName("zutubi.dashboardConfig")
@Form(fieldOrder = {"showAllProjects", "sortProjectsAlphabetically", "shownProjects", "groupsShown", "showUngrouped", "showAllGroups", "sortGroupsAlphabetically", "shownGroups", "hierarchyShown", "hiddenHierarchyLevels", "buildsPerProject", "columns", "showMyChanges", "myChangeCount", "showProjectChanges", "projectChangeCount"})
@Classification(single = "dashboard")
public class DashboardConfiguration extends ProjectsSummaryConfiguration
{
    @ControllingCheckbox(uncheckedFields = {"shownProjects", "sortProjectsAlphabetically"})
    private boolean showAllProjects = true;
    /**
     * Projects to show on the dashboard.
     */
    @Reference
    private List<ProjectConfiguration> shownProjects = new LinkedList<ProjectConfiguration>();
    private boolean sortProjectsAlphabetically = true;

    @ControllingCheckbox(checkedFields = {"showAllGroups", "showUngrouped"})
    private boolean groupsShown = true;
    @ControllingCheckbox(uncheckedFields = {"shownGroups", "sortGroupsAlphabetically"})
    private boolean showAllGroups = true;
    private boolean sortGroupsAlphabetically = true;

    /**
     * Project groups to show on dashboard.
     */
    @ItemPicker(optionProvider = "com.zutubi.pulse.master.tove.config.project.ProjectLabelOptionProvider")
    private List<String> shownGroups = new LinkedList<String>();
    private boolean showUngrouped = true;
    @ControllingCheckbox(checkedFields = {"myChangeCount"})
    private boolean showMyChanges = true;
    /**
     * The number of recent changes by this user to show.
     */
    private int myChangeCount = 10;

    @ControllingCheckbox(checkedFields = {"projectChangeCount"})
    private boolean showProjectChanges = true;
    /**
     * The number of recent changes to this user's projects to show.
     */
    private int projectChangeCount = 10;

    public DashboardConfiguration()
    {
        setPermanent(true);
    }

    public boolean isShowAllProjects()
    {
        return showAllProjects;
    }

    public void setShowAllProjects(boolean showAllProjects)
    {
        this.showAllProjects = showAllProjects;
    }

    public List<ProjectConfiguration> getShownProjects()
    {
        return shownProjects;
    }

    public void setShownProjects(List<ProjectConfiguration> shownProjects)
    {
        this.shownProjects = shownProjects;
    }

    public boolean isGroupsShown()
    {
        return groupsShown;
    }

    public void setGroupsShown(boolean groupsShown)
    {
        this.groupsShown = groupsShown;
    }

    public boolean isShowAllGroups()
    {
        return showAllGroups;
    }

    public void setShowAllGroups(boolean showAllGroups)
    {
        this.showAllGroups = showAllGroups;
    }

    public List<String> getShownGroups()
    {
        return shownGroups;
    }

    public void setShownGroups(List<String> shownGroups)
    {
        this.shownGroups = shownGroups;
    }

    public boolean isShowUngrouped()
    {
        return showUngrouped;
    }

    public void setShowUngrouped(boolean showUngrouped)
    {
        this.showUngrouped = showUngrouped;
    }

    public boolean isShowMyChanges()
    {
        return showMyChanges;
    }

    public void setShowMyChanges(boolean showMyChanges)
    {
        this.showMyChanges = showMyChanges;
    }

    public int getMyChangeCount()
    {
        return myChangeCount;
    }

    public void setMyChangeCount(int myChangeCount)
    {
        this.myChangeCount = myChangeCount;
    }

    public boolean isShowProjectChanges()
    {
        return showProjectChanges;
    }

    public void setShowProjectChanges(boolean showProjectChanges)
    {
        this.showProjectChanges = showProjectChanges;
    }

    public int getProjectChangeCount()
    {
        return projectChangeCount;
    }

    public void setProjectChangeCount(int projectChangeCount)
    {
        this.projectChangeCount = projectChangeCount;
    }

    public boolean isSortProjectsAlphabetically()
    {
        return sortProjectsAlphabetically;
    }

    public void setSortProjectsAlphabetically(boolean sortProjectsAlphabetically)
    {
        this.sortProjectsAlphabetically = sortProjectsAlphabetically;
    }

    public boolean isSortGroupsAlphabetically()
    {
        return sortGroupsAlphabetically;
    }

    public void setSortGroupsAlphabetically(boolean sortGroupsAlphabetically)
    {
        this.sortGroupsAlphabetically = sortGroupsAlphabetically;
    }
}
