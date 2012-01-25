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
