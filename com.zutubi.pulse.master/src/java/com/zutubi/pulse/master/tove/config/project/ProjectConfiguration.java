package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.config.annotations.*;
import com.zutubi.pulse.core.config.*;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.master.tove.config.project.commit.CommitMessageTransformerConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.TypeConfiguration;
import com.zutubi.tove.type.Extendable;
import com.zutubi.validation.annotations.Url;

import java.util.*;

/**
 * A project defines how and when to run a build.  It is the fundamental unit
 * of configuration for builds.  Note that a "software project" is likely to
 * be represented by many projects in Pulse.  For example, there may be one
 * Pulse project for a nightly build and another for a continuous build.
 */
@Form(fieldOrder = {"name", "url", "description"})
@Listing(order = {"type", "requirements", "properties", "stages", "options", "buildHooks", "scm", "changeViewer", "commitMessageTransformers", "labels", "permissions"})
@Table(columns = {"name"})
@SymbolicName("zutubi.projectConfig")
public class ProjectConfiguration extends AbstractConfiguration implements Extendable, NamedConfiguration
{
    @ExternalState
    private long projectId;
    /**
     * Note that we manage the name ourselves (rather than extending {@link
     * AbstractNamedConfiguration}) so we can tag it with @NoInherit.
     */
    @NoInherit
    private String name;
    @Url
    private String url;
    @NoInherit
    @TextArea(rows = 7, cols = 70)
    private String description;
    @Essential
    private ScmConfiguration scm;
    @Essential
    private TypeConfiguration type;

    @Ordered
    private Map<String, ResourceProperty> properties = new LinkedHashMap<String, ResourceProperty>();

    private BuildOptionsConfiguration options = new BuildOptionsConfiguration();

    @Ordered
    private Map<String, BuildStageConfiguration> stages = new LinkedHashMap<String, BuildStageConfiguration>();

    private List<ResourceRequirement> requirements = new LinkedList<ResourceRequirement>();

    private List<LabelConfiguration> labels = new LinkedList<LabelConfiguration>();

    private List<ProjectAclConfiguration> permissions = new LinkedList<ProjectAclConfiguration>();

    @Transient
    private Map<String, Object> extensions = new HashMap<String, Object>();

    @Ordered
    private Map<String, BuildHookConfiguration> buildHooks = new LinkedHashMap<String, BuildHookConfiguration>();

    @Ordered
    private Map<String, CommitMessageTransformerConfiguration> commitMessageTransformers = new LinkedHashMap<String, CommitMessageTransformerConfiguration>();

    private ChangeViewerConfiguration changeViewer;

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

    public Map<String, BuildHookConfiguration> getBuildHooks()
    {
        return buildHooks;
    }

    public void setBuildHooks(Map<String, BuildHookConfiguration> buildHooks)
    {
        this.buildHooks = buildHooks;
    }

    public Map<String, CommitMessageTransformerConfiguration> getCommitMessageTransformers()
    {
        return commitMessageTransformers;
    }

    public void setCommitMessageTransformers(Map<String, CommitMessageTransformerConfiguration> commitMessageTransformers)
    {
        this.commitMessageTransformers = commitMessageTransformers;
    }

    public ChangeViewerConfiguration getChangeViewer()
    {
        return changeViewer;
    }

    public void setChangeViewer(ChangeViewerConfiguration changeViewer)
    {
        this.changeViewer = changeViewer;
    }

    public List<LabelConfiguration> getLabels()
    {
        return labels;
    }

    public void setLabels(List<LabelConfiguration> labels)
    {
        this.labels = labels;
    }

    public List<ProjectAclConfiguration> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(List<ProjectAclConfiguration> permissions)
    {
        this.permissions = permissions;
    }

    public void addPermission(ProjectAclConfiguration permission)
    {
        permissions.add(permission);
    }
}
