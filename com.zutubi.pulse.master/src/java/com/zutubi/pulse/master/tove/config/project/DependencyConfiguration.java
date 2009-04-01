package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

import java.util.List;

/**
 * A dependency defines a project and the artifacts built by that project that this project requires
 * for building. 
 */
@SymbolicName("zutubi.dependency")
@Table(columns = {"module", "revision", "stages", "transitive"})
@Form(fieldOrder = {"project", "revision", "transitive", "allStages", "stages"})
public class DependencyConfiguration extends AbstractConfiguration
{
    public static final String ALL_STAGES = "*";
    public static final String LATEST_INTEGRATION = "latest.integration";

    /**
     * The organisation name of the dependency.
     */
    @Transient // not implemented as a separate concept at this stage.
    private String org;

    /**
     * The project being depended upon.
     */
    @Required @Reference
    private ProjectConfiguration project;

    /**
     * The revision of this dependency.
     */
    @Required
    private String revision = LATEST_INTEGRATION;

    /**
     * Indicates whether or not to resolve this dependencies dependencies.
     */
    private boolean transitive = true;

    @ControllingCheckbox(invert = true, dependentFields = {"stages"})
    private boolean allStages = true;

    @Reference @Select(optionProvider = "DependencyStagesOptionProvider") 
    private List<BuildStageConfiguration> stages = null;

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
}
