package com.zutubi.pulse.master.project.events;

import com.zutubi.events.Event;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * Base class for all project events.
 */
public abstract class ProjectEvent extends Event
{
    private ProjectConfiguration projectConfiguration;

    /**
     * Create a new project event.
     *
     * @param source               {@inheritDoc}
     * @param projectConfiguration project that the event relates to
     */
    public ProjectEvent(Object source, ProjectConfiguration projectConfiguration)
    {
        super(source);
        this.projectConfiguration = projectConfiguration;
    }

    /**
     * @return the project to which this event relates
     */
    public ProjectConfiguration getProjectConfiguration()
    {
        return projectConfiguration;
    }
}
