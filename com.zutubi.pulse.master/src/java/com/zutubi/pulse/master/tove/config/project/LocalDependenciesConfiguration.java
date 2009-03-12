package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.SymbolicName;

import java.util.List;
import java.util.LinkedList;

/**
 * The manually configured dependencies.
 */
@SymbolicName("zutubi.localDependenciesConfiguration")
public class LocalDependenciesConfiguration extends DependenciesConfiguration
{
    private List<DependencyConfiguration> dependencies = new LinkedList<DependencyConfiguration>();

    public List<DependencyConfiguration> getDependencies()
    {
        return dependencies;
    }

    public void setDependencies(List<DependencyConfiguration> dependencies)
    {
        this.dependencies = dependencies;
    }
}
