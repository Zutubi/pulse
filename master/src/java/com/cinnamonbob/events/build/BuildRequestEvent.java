package com.cinnamonbob.events.build;

import com.cinnamonbob.events.Event;
import com.cinnamonbob.model.Project;

/**
 */
public class BuildRequestEvent extends Event
{
    private Project project;
    private String specification;

    public BuildRequestEvent(Object source, Project project, String specification)
    {
        super(source);
        this.project = project;
        this.specification = specification;
    }

    public Project getProject()
    {
        return project;
    }

    public String getSpecification()
    {
        return specification;
    }
}
