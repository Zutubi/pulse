package com.cinnamonbob.core2.task;

import com.cinnamonbob.core2.Project;

/**
 * 
 *
 */
public abstract class AbstractTask implements Task
{
    private Project project;

    public Project getProject()
    {
        return project;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }
}
