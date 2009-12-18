package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.core.postprocessors.api.PostProcessorConfiguration;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.master.tove.config.LabelConfiguration;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.pulse.master.tove.config.project.commit.CommitMessageTransformerConfiguration;
import com.zutubi.pulse.master.tove.config.project.hooks.BuildHookConfiguration;
import com.zutubi.pulse.master.tove.config.project.reports.ReportGroupConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.TypeConfiguration;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.tove.config.api.NamedConfiguration;
import com.zutubi.tove.type.Extendable;
import com.zutubi.validation.annotations.Url;

import java.util.*;

/**
 * A project defines how and when to run a build.  It is the fundamental unit
 * of configuration for builds.  Note that a "software project" is likely to
 * be represented by many projects in Pulse.  For example, there may be one
 * Pulse project for a nightly build and another for a continuous build.
 */
@Form(fieldOrder = {"name", "organisation", "url", "description"})
@Listing(order = {"type", "requirements", "properties", "stages", "options", "triggers", "buildHooks", "scm", "changeViewer", "commitMessageTransformers", "labels", "contacts", "permissions", "reportGroups", "cleanupRules"})
@Table(columns = {"name"})
@SymbolicName("zutubi.projectConfig")
public class ProjectConfiguration extends AbstractConfiguration implements Extendable, NamedConfiguration
{
    @ExternalState
    private long projectId;
    /**
     * Note that we manage the name ourselves (rather than extending {@link
     * com.zutubi.tove.config.api.AbstractNamedConfiguration}) so we can tag it with @NoInherit.
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
    private Map<String, PostProcessorConfiguration> postProcessors = new HashMap<String, PostProcessorConfiguration>();

    @Wizard.Ignore
    private String organisation;

    private DependenciesConfiguration dependencies = new DependenciesConfiguration();

    @Ordered
    private Map<String, ResourcePropertyConfiguration> properties = new LinkedHashMap<String, ResourcePropertyConfiguration>();

    private BuildOptionsConfiguration options = new BuildOptionsConfiguration();

    @Ordered
    private Map<String, BuildStageConfiguration> stages = new LinkedHashMap<String, BuildStageConfiguration>();

    private List<ResourceRequirementConfiguration> requirements = new LinkedList<ResourceRequirementConfiguration>();

    private List<LabelConfiguration> labels = new LinkedList<LabelConfiguration>();

    private List<ProjectAclConfiguration> permissions = new LinkedList<ProjectAclConfiguration>();

    private Map<String, Object> extensions = new HashMap<String, Object>();

    @Ordered
    private Map<String, BuildHookConfiguration> buildHooks = new LinkedHashMap<String, BuildHookConfiguration>();

    @Ordered
    private Map<String, CommitMessageTransformerConfiguration> commitMessageTransformers = new LinkedHashMap<String, CommitMessageTransformerConfiguration>();

    private ChangeViewerConfiguration changeViewer;

    @Ordered
    private Map<String, ReportGroupConfiguration> reportGroups = new LinkedHashMap<String, ReportGroupConfiguration>();

    private ProjectContactsConfiguration contacts = new ProjectContactsConfiguration();

    public ProjectConfiguration()
    {
    }

    public ProjectConfiguration(String name)
    {
        this.name = name;
    }

    public ProjectConfiguration(String organisation, String name)
    {
        this.organisation = organisation;
        this.name = name;
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

    public Map<String, ResourcePropertyConfiguration> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, ResourcePropertyConfiguration> properties)
    {
        this.properties = properties;
    }

    public ResourcePropertyConfiguration getProperty(String name)
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

    public void addExtension(String name, Object extension)
    {
        getExtensions().put(name, extension);
    }

    public BuildOptionsConfiguration getOptions()
    {
        return options;
    }

    public void setOptions(BuildOptionsConfiguration options)
    {
        this.options = options;
    }

    public List<ResourceRequirementConfiguration> getRequirements()
    {
        return requirements;
    }

    public void setRequirements(List<ResourceRequirementConfiguration> requirements)
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

    public Map<String, PostProcessorConfiguration> getPostProcessors()
    {
        return postProcessors;
    }

    public void setPostProcessors(Map<String, PostProcessorConfiguration> postProcessors)
    {
        this.postProcessors = postProcessors;
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

    public DependenciesConfiguration getDependencies()
    {
        return dependencies;
    }

    public void setDependencies(DependenciesConfiguration dependencies)
    {
        this.dependencies = dependencies;
    }

    public boolean hasDependencies()
    {
        return dependencies != null && dependencies.getDependencies() != null && dependencies.getDependencies().size() > 0;
    }

    /**
     * Returns true if this project is dependent on the other project.
     *
     * @param other another project
     * @return true iff this project is dependent on the other project.
     */
    public boolean isDependentOn(ProjectConfiguration other)
    {
        if (hasDependencies())
        {
            for (DependencyConfiguration dependency : dependencies.getDependencies())
            {
                ProjectConfiguration dependent = dependency.getProject();
                if (dependent.equals(other) || dependent.isDependentOn(other))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public String getOrganisation()
    {
        return organisation;
    }

    public void setOrganisation(String organisation)
    {
        this.organisation = organisation;
    }

    public Map<String, ReportGroupConfiguration> getReportGroups()
    {
        return reportGroups;
    }

    public void setReportGroups(Map<String, ReportGroupConfiguration> reportGroups)
    {
        this.reportGroups = reportGroups;
    }

    public void addReportGroup(ReportGroupConfiguration reportGroup)
    {
        reportGroups.put(reportGroup.getName(), reportGroup);
    }

    public ProjectContactsConfiguration getContacts()
    {
        return contacts;
    }

    public void setContacts(ProjectContactsConfiguration contacts)
    {
        this.contacts = contacts;
    }
}
