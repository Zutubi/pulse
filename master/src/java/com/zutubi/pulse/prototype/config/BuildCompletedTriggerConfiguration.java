package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.annotation.Form;
import com.zutubi.prototype.annotation.Reference;
import com.zutubi.prototype.annotation.Select;
import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.prototype.CompletedResultStateOptionProvider;
import com.zutubi.validation.annotations.Required;

import java.util.List;

/**
 */
@Form(fieldOrder = { "name", "project", "states"})
public class BuildCompletedTriggerConfiguration extends TriggerConfiguration
{
    @Reference @Required
    private ProjectConfiguration project;
    @Reference
    private List<ProjectConfiguration> projects;
    @Select(optionProvider = CompletedResultStateOptionProvider.class)
    private List<ResultState> states;

    public BuildCompletedTriggerConfiguration()
    {
    }

    public ProjectConfiguration getProject()
    {
        return project;
    }

    public void setProject(ProjectConfiguration project)
    {
        this.project = project;
    }

    public List<ProjectConfiguration> getProjects()
    {
        return projects;
    }

    public void setProjects(List<ProjectConfiguration> projects)
    {
        this.projects = projects;
    }

    public List<ResultState> getStates()
    {
        return states;
    }

    public void setStates(List<ResultState> states)
    {
        this.states = states;
    }
}
