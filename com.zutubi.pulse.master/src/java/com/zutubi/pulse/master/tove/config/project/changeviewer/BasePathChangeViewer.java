package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Wire;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Url;

/**
 * A type of change viewer that can be configured using a base URL and a
 * project path.
 */
@Wire
@SymbolicName("zutubi.basePathChangeViewer")
public abstract class BasePathChangeViewer extends ChangeViewerConfiguration
{
    @Required
    @Url
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
