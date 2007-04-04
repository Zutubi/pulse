package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.Formatter;

/**
 *
 *
 */
public class ProjectConfigurationFormatter implements Formatter<ProjectConfiguration>
{
    public String format(ProjectConfiguration obj)
    {
        return obj.getName();
    }
}
