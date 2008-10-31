package com.zutubi.pulse.master.project.events;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

/**
 * Event used to log a freeform status message for a project.
 */
public class ProjectStatusEvent extends ProjectEvent
{
    private String message;

    /**
     * Creates a new freeform status event for a project.
     *
     * @param source               {@inheritDoc}
     * @param projectConfiguration {@inheritDoc}
     * @param message              the status message to be reported, should
     *                             consist of a single line of status
     */
    public ProjectStatusEvent(Object source, ProjectConfiguration projectConfiguration, String message)
    {
        super(source, projectConfiguration);
        this.message = message;
    }

    /**
     * @return the status message, a single line of project feedback
     */
    public String getMessage()
    {
        return message;
    }

    public String toString()
    {
        return "Project Status Event: " + getProjectConfiguration().getName() + ": " + message;
    }
}
