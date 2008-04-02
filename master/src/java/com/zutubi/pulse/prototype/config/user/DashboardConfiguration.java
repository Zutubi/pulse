package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;

import java.util.LinkedList;
import java.util.List;

/**
 * User preferences controlling what they see on their dashboard.
 */
@SymbolicName("zutubi.dashboardConfig")
@Form(labelWidth = 300, fieldOrder = {"buildCount", "showAllProjects", "shownProjects", "showMyChanges", "myChangeCount", "showProjectChanges", "projectChangeCount"})
@Classification(single = "dashboard")
public class DashboardConfiguration extends AbstractConfiguration
{
    /**
     * Number of builds to show for each project.
     */
    private int buildCount = 3;

    @ControllingCheckbox(invert = true, dependentFields = {"shownProjects"})
    private boolean showAllProjects = true;
    /**
     * Projects to show on the dashboard.
     */
    @Reference
    private List<ProjectConfiguration> shownProjects = new LinkedList<ProjectConfiguration>();
    @ControllingCheckbox(invert = true, dependentFields = {"shownGroups"})
    private boolean showAllGroups = true;
    /**
     * Project groups to show on dashboard.
     */
    @ItemPicker(optionProvider = "com.zutubi.pulse.prototype.config.project.ProjectLabelOptionProvider")
    private List<String> shownGroups = new LinkedList<String>();

    @ControllingCheckbox(dependentFields = {"myChangeCount"})
    private boolean showMyChanges = true;
    /**
     * The number of recent changes by this user to show.
     */
    private int myChangeCount = 10;

    @ControllingCheckbox(dependentFields = {"projectChangeCount"})
    private boolean showProjectChanges = true;
    /**
     * The number of recent changes to this user's projects to show.
     */
    private int projectChangeCount = 10;

    public DashboardConfiguration()
    {
        setPermanent(true);
    }

    public int getBuildCount()
    {
        return buildCount;
    }

    public void setBuildCount(int buildCount)
    {
        this.buildCount = buildCount;
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
}
