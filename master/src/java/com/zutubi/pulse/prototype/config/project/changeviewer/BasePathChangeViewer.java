package com.zutubi.pulse.prototype.config.project.changeviewer;

import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.prototype.config.ConfigurationProvider;

/**
 * A type of change viewer that can be configured using a base URL and a
 * project path.
 */
public abstract class BasePathChangeViewer extends ChangeViewerConfiguration
{
    private String baseURL;
    private String projectPath;
    protected ConfigurationProvider configurationProvider = null;

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

    public boolean hasCapability(Capability capability)
    {
        return true;
    }

    protected ScmConfiguration lookupScmConfiguration()
    {
        ProjectConfiguration project = configurationProvider.getAncestorOfType(this, ProjectConfiguration.class);
        return project.getScm();
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
