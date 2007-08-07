package com.zutubi.pulse.prototype.config.project;

import com.zutubi.config.annotations.*;
import com.zutubi.prototype.type.Extendable;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.pulse.core.config.NamedConfiguration;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.model.ResourceRequirement;
import com.zutubi.pulse.prototype.config.project.actions.PostBuildActionConfiguration;
import com.zutubi.pulse.prototype.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.prototype.config.project.types.TypeConfiguration;
import com.zutubi.pulse.scm.config.ScmConfiguration;
import com.zutubi.validation.annotations.Url;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
@Form(fieldOrder = {"name", "url", "description"})
@Table(columns = {"name"})
@SymbolicName("zutubi.projectConfig")
public class ProjectConfiguration extends AbstractConfiguration implements Extendable, NamedConfiguration
{
    @Internal
    private long projectId;
    @NoInherit
    private String name;
    @Url
    private String url;
    @NoInherit
    @TextArea(rows = 7, cols = 70)
    private String description;

    private ScmConfiguration scm;

    private TypeConfiguration type;
    
    private Map<String, ResourceProperty> properties;

    private BuildOptionsConfiguration options;

    private Map<String, BuildStageConfiguration> stages;

    private List<ResourceRequirement> requirements;

    @Transient
    private Map<String, Object> extensions = new HashMap<String, Object>();

    @Transient // FIXME sort this out later.
    private List<PostBuildActionConfiguration> postBuildActions = new LinkedList<PostBuildActionConfiguration>();

    private ChangeViewerConfiguration changeViewer;

    public ProjectConfiguration()
    {
        // setup defaults
        options = new BuildOptionsConfiguration();

        BuildStageConfiguration defaultStage = new BuildStageConfiguration();
        defaultStage.setName("default");
        stages = new HashMap<String, BuildStageConfiguration>();
        stages.put("default", defaultStage);
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

    public Map<String, BuildStageConfiguration> getStages()
    {
        return stages;
    }

    public void setStages(Map<String, BuildStageConfiguration> stages)
    {
        this.stages = stages;
    }

    public BuildStageConfiguration getStage(String name)
    {
        return stages.get(name);
    }

    public Map<String, Object> getExtensions()
    {
        return extensions;
    }

    public BuildOptionsConfiguration getOptions()
    {
        return options;
    }

    public void setOptions(BuildOptionsConfiguration options)
    {
        this.options = options;
    }

    public List<ResourceRequirement> getRequirements()
    {
        return requirements;
    }

    public void setRequirements(List<ResourceRequirement> requirements)
    {
        this.requirements = requirements;
    }

    public TypeConfiguration getType()
    {
        return type;
    }

    public void setType(TypeConfiguration type)
    {
        this.type = type;
    }

    public List<PostBuildActionConfiguration> getPostBuildActions()
    {
        return postBuildActions;
    }

    public void setPostBuildActions(List<PostBuildActionConfiguration> postBuildActions)
    {
        this.postBuildActions = postBuildActions;
    }

    public ChangeViewerConfiguration getChangeViewer()
    {
        return changeViewer;
    }

    public void setChangeViewer(ChangeViewerConfiguration changeViewer)
    {
        this.changeViewer = changeViewer;
    }
}
