package com.zutubi.pulse.master.project.events;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * A request to clean build directories for a project.
 */
public class ProjectDirectoryCleanupRequestEvent extends ProjectEvent
{
    /**
     * Create an event to request cleanup of a project's build directories.
     *
     * @param source               {@inheritDoc}
     * @param projectConfiguration {@inheritDoc}
     */
    public ProjectDirectoryCleanupRequestEvent(Object source, ProjectConfiguration projectConfiguration)
    {
        super(source, projectConfiguration);
    }

    public String toString()
    {
        return "Project Directory Cleanup Request: " + getProjectConfiguration().getName();
    }
}
