package com.zutubi.pulse.prototype.config.types;

import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.config.annotations.SymbolicName;

/**
 *
 *
 */
@SymbolicName("internal.xcodeTypeConfig")
public class XCodeTypeConfiguration extends AbstractConfiguration
{
    private String workingDir = null;
    private String config = null;
    private String project = null;
    private String target = null;
    private String action = null;
    private String settings = null;

    public String getWorkingDir()
    {
        return workingDir;
    }

    public void setWorkingDir(String workingDir)
    {
        this.workingDir = workingDir;
    }

    public String getConfig()
    {
        return config;
    }

    public void setConfig(String config)
    {
        this.config = config;
    }

    public String getProject()
    {
        return project;
    }

    public void setProject(String project)
    {
        this.project = project;
    }

    public String getTarget()
    {
        return target;
    }

    public void setTarget(String target)
    {
        this.target = target;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getSettings()
    {
        return settings;
    }

    public void setSettings(String settings)
    {
        this.settings = settings;
    }
}
