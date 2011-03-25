package com.zutubi.pulse.master.project.events;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * Destruction has just commenced for a project.
 */
public class ProjectDestructionCommencedEvent extends ProjectLifecycleEvent
{
    /**
     * Create an event to indicate that destruction has started for a
     * project.
     *
     * @param source               {@inheritDoc}
     * @param projectConfiguration {@inheritDoc}
     */
    public ProjectDestructionCommencedEvent(Object source, ProjectConfiguration projectConfiguration)
    {
        super(source, projectConfiguration);
    }

    public String toString()
    {
        return "Project Destruction Commenced: " + getProjectConfiguration().getName();
    }
}