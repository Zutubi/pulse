package com.zutubi.pulse.master.project.events;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * Destruction has just completed for a project.  If it failed, there may be
 * an error message.
 */
public class ProjectDestructionCompletedEvent extends ProjectLifecycleEvent
{
    /**
     * Create an event to indicate that destructiontion has completed for a
     * project.
     *
     * @param source               {@inheritDoc}
     * @param projectConfiguration {@inheritDoc}
     */
    public ProjectDestructionCompletedEvent(Object source, ProjectConfiguration projectConfiguration)
    {
        super(source, projectConfiguration);
    }

    public String toString()
    {
        return "Project Destruction Completed: " + getProjectConfiguration().getName();
    }
}