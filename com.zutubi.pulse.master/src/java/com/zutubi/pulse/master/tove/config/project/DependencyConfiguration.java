package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Required;

/**
 *
 */
@SymbolicName("zutubi.dependency")
@Table(columns = {/*"org", */"module", "revision"})
@Form(fieldOrder = {"project", "revision", "stages", "transitive"})
public class DependencyConfiguration extends AbstractConfiguration
{
    /**
     * The organisation name of the dependency.
     */
//    @Required
    @Transient // not implemented as a separate concept yet.
    private String org;

    /**
     * The module name of the dependency, a unique within an organisation.
     */
/*
    @Required
    private String module;
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
    private boolean transitive = false;

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
