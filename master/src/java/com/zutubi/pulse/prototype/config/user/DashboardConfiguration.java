package com.zutubi.pulse.prototype.config.user;

import com.zutubi.config.annotations.ControllingCheckbox;
import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Reference;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;

import java.util.List;

/**
 *
 *
 */
@SymbolicName("zutubi.dashboardConfig")
@Form(fieldOrder = {"buildCount", "showAllProjects", "shownProjects", "showMyChanges", "myChangeCount", "showProjectChanges", "projectChangeCount"})
public class DashboardConfiguration extends AbstractConfiguration
{
    private int buildCount;

    @ControllingCheckbox(invert = true, dependentFields = {"shownProjects"})
    private boolean showAllProjects;

    @Reference
    private List<ProjectConfiguration> shownProjects;

    @ControllingCheckbox(dependentFields = {"myChangeCount"})
    private boolean showMyChanges;

    private int myChangeCount;

    @ControllingCheckbox(dependentFields = {"projectChangeCount"})
    private boolean showProjectChanges;
    
    private int projectChangeCount;

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
