package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 * A dependency defines a project and the artifacts built by that project that this project requires
 * for building. 
 */
@SymbolicName("zutubi.dependency")
@Table(columns = {"module", "revision"})
@Form(fieldOrder = {"project", "revision", "stages", "transitive"})
public class DependencyConfiguration extends AbstractConfiguration
{
    /**
     * The organisation name of the dependency.
     */
    @Transient // not implemented as a separate concept yet.
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
    private String revision = "latest.integration";

    /**
     * Indicates whether or not to resolve this dependencies dependencies.
     */
    private boolean transitive = true;

    private String stages = "*";

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
