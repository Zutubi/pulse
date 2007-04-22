package com.zutubi.pulse.model;

import com.zutubi.pulse.servercore.config.ScmConfiguration;

/**
 * A type of change viewer that can be configured using a base URL and a
 * project path.
 */
public abstract class BasePathChangeViewer extends ChangeViewer
{
    private String baseURL;
    private String projectPath;

    protected BasePathChangeViewer(String baseURL, String projectPath)
    {
        this.baseURL = baseURL;
        this.projectPath = projectPath;
    }

    public String getBaseURL()
    {
        return baseURL;
    }

    public void setBaseURL(String baseURL)
    {
        this.baseURL = baseURL;
    }

    public String getProjectPath()
    {
        return projectPath;
    }

    public void setProjectPath(String projectPath)
    {
        this.projectPath = projectPath;
    }

    public boolean hasCapability(ScmConfiguration scm, Capability capability)
    {
        return true;
    }
}
