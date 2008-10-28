package com.zutubi.pulse.master.project.events;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * Initialisation has just commenced for a project.
 */
public class ProjectInitialisationCommencedEvent extends ProjectLifecycleEvent
{
    /**
     * Create an event to indicate that initialisation has started for a
     * project.
     *
     * @param source               {@inheritDoc}
     * @param projectConfiguration {@inheritDoc}
     */
    public ProjectInitialisationCommencedEvent(Object source, ProjectConfiguration projectConfiguration)
    {
        super(source, projectConfiguration);
    }

    public String toString()
    {
        return "Project Initialisation Commenced: " + getProjectConfiguration().getName();
    }
}
