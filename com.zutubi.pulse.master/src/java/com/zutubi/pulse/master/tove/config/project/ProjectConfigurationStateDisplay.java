package com.zutubi.pulse.master.tove.config.project;

/**
 * Formats display fields for projects.
 */
public class ProjectConfigurationStateDisplay
{
    public String formatHandle(ProjectConfiguration config)
    {
        return Long.toString(config.getHandle());
    }

    public String formatProjectId(ProjectConfiguration config)
    {
        return Long.toString(config.getProjectId());
    }
}
