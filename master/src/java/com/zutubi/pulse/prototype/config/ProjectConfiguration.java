package com.zutubi.pulse.prototype.config;

import com.zutubi.config.annotations.*;
import com.zutubi.validation.annotations.Url;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.servercore.config.ScmConfiguration;
import com.zutubi.prototype.type.Extendable;

import java.util.Map;
import java.util.HashMap;

/**
 *
 *
 */
@Form(fieldOrder = {"name", "url", "description"})
@Wizard("com.zutubi.prototype.wizard.webwork.ConfigureProjectWizard")
@Format("ProjectConfigurationFormatter")
public class ProjectConfiguration extends AbstractNamedConfiguration implements Extendable
{
    @Internal
    private long projectId;
    @Url
    private String url;
    @TextArea
    private String description;
    private ScmConfiguration scm;
    private Map<String, ResourceProperty> properties;

    @Transient
    private Map<String, Object> extensions = new HashMap<String, Object>();

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public ScmConfiguration getScm()
    {
        return scm;
    }

    public void setScm(ScmConfiguration scm)
    {
        this.scm = scm;
    }

    public Map<String, ResourceProperty> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, ResourceProperty> properties)
    {
        this.properties = properties;
    }

    public ResourceProperty getProperty(String name)
    {
        return properties.get(name);
    }

    public Map<String, Object> getExtensions()
    {
        return extensions;
    }

    
}
