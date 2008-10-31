package com.zutubi.pulse.master.project.events;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * Events regarding project lifecycle: initialisation and cleanup.
 */
public abstract class ProjectLifecycleEvent extends ProjectEvent
{
    /**
     * Create an event indicating a lifecycle change for a project.
     *
     * @param source               {@inheritDoc}
     * @param projectConfiguration {@inheritDoc}
     */
    public ProjectLifecycleEvent(Object source, ProjectConfiguration projectConfiguration)
    {
        super(source, projectConfiguration);
    }
}
