package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * A dependency defines a project and the artifacts built by that project that this project requires
 * for building. 
 */
@SymbolicName("zutubi.dependency")
@Table(columns = {"module", "revision", "stages", "transitive"})
@Form(fieldOrder = {"project", "revision", "stages", "transitive"})
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

    @Select(optionProvider = "DependencyStagesOptionProvider")
    private String stages = ALL_STAGES;

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

    public String getStages()
    {
        return stages;
    }

    public void setStages(String stages)
    {
        this.stages = stages;
    }
}
