package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;
import com.zutubi.pulse.core.dependency.ivy.IvyManager;

import java.util.List;
import java.util.LinkedList;

/**
 * A dependency defines a project and the artifacts built by that project that this project requires
 * for building. 
 */
@SymbolicName("zutubi.dependency")
@Table(columns = {"module", "revision", "stages", "transitive"})
@Form(fieldOrder = {"project", "revision", "customRevision", "transitive", "allStages", "stages"})
public class DependencyConfiguration extends AbstractConfiguration
{
    private static final String LATEST = "latest.";

    public static final String ALL_STAGES = "*";
    public static final String REVISION_LATEST_INTEGRATION = LATEST + IvyManager.STATUS_INTEGRATION;
    public static final String REVISION_LATEST_MILESTONE = LATEST + IvyManager.STATUS_MILESTONE;
    public static final String REVISION_LATEST_RELEASE = LATEST + IvyManager.STATUS_RELEASE;
    public static final String REVISION_CUSTOM = "custom";

    /**
     * The organisation name of the dependency.
     */
    @Transient // not implemented as a separate concept at this stage.
    private String org;

    /**
     * The project being depended upon.
     */
    @Required @Reference(optionProvider = "DependencyProjectOptionProvider")
    private ProjectConfiguration project;

    /**
     * The revision of this dependency.
     */
    @Required
    @ControllingSelect(enableSet = {REVISION_CUSTOM}, dependentFields = "customRevision", optionProvider = "DependencyConfigurationRevisionOptionProvider")
    private String revision = REVISION_LATEST_INTEGRATION;

    /**
     * The custom revision, used if the revision field is set to custom.
     */
    @Required
    private String customRevision = "";

    /**
     * Indicates whether or not to resolve this dependencies dependencies.
     */
    private boolean transitive = true;

    @ControllingCheckbox(invert = true, dependentFields = {"stages"})
    private boolean allStages = true;

    @Reference(dependentOn = "project", optionProvider = "DependencyStagesOptionProvider")
    private List<BuildStageConfiguration> stages = new LinkedList<BuildStageConfiguration>();

    public String getOrg()
    {
        return org;
    }

    public void setOrg(String org)
    {
        this.org = org;
    }

    @Transient
    public String getModule()
    {
        return project.getName();
    }

    public ProjectConfiguration getProject()
    {
        return project;
    }

    public void setProject(ProjectConfiguration project)
    {
        this.project = project;
    }

    public String getRevision()
    {
        return revision;
    }

    public void setRevision(String revision)
    {
        this.revision = revision;
    }

    public boolean isTransitive()
    {
        return transitive;
    }

    public void setTransitive(boolean transitive)
    {
        this.transitive = transitive;
    }

    public List<BuildStageConfiguration> getStages()
    {
        return stages;
    }

    public void setStages(List<BuildStageConfiguration> stages)
    {
        this.stages = stages;
    }

    public boolean isAllStages()
    {
        return allStages;
    }

    public void setAllStages(boolean allStages)
    {
        this.allStages = allStages;
    }

    public String getCustomRevision()
    {
        return customRevision;
    }

    public void setCustomRevision(String customRevision)
    {
        this.customRevision = customRevision;
    }

    public String getDependencyRevision()
    {
        if (revision != null && revision.equals(REVISION_CUSTOM))
        {
            return customRevision;
        }
        return revision;
    }
}
