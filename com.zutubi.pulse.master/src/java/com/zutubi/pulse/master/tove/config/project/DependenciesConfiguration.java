package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.Select;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractConfiguration;
import com.zutubi.validation.annotations.Constraint;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;

/**
 * The manually configured dependencies.
 */
@SymbolicName("zutubi.dependenciesConfiguration")
@Form(fieldOrder = {"version", "status", "publicationPattern", "retrievalPattern"})
public class DependenciesConfiguration extends AbstractConfiguration
{
    // We don't look up Ivy's definition as it causes early loading of ivy
    // settings (before, e.g., we have a chance to redirect logging).
    private static final String STATUS_INTEGRATION = "integration";

    private List<DependencyConfiguration> dependencies = new LinkedList<DependencyConfiguration>();

    @Required
    @Constraint("com.zutubi.pulse.core.dependency.ivy.IvyPatternValidator")
    private String retrievalPattern = "lib/[artifact].[ext]";

    @Required
    @Constraint("com.zutubi.pulse.core.dependency.StatusValidator")
    @Select(optionProvider = "com.zutubi.pulse.master.tove.config.project.BuildStatusOptionProvider")
    private String status = STATUS_INTEGRATION;

    @Required
    private String version = "${build.number}";

    public List<DependencyConfiguration> getDependencies()
    {
        return dependencies;
    }

    public void setDependencies(List<DependencyConfiguration> dependencies)
    {
        this.dependencies = dependencies;
    }

    public String getRetrievalPattern()
    {
        return retrievalPattern;
    }

    public void setRetrievalPattern(String retrievalPattern)
    {
        this.retrievalPattern = retrievalPattern;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
}
