package com.zutubi.pulse.prototype.config;

import com.zutubi.prototype.Formatter;

/**
 *
 *
 */
public class BuildStageConfigurationFormatter implements Formatter<BuildStageConfiguration>
{
    public String format(BuildStageConfiguration obj)
    {
        return obj.getName();
    }
}
