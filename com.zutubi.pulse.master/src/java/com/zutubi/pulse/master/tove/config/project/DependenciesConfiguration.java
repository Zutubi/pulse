package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractConfiguration;

import java.util.List;

/**
 * The base configuration instance that defines the source of the projects dependencies.
 * This class to be extended by implementations that know where the dependency information
 * is defined. 
 */
@SymbolicName("zutubi.dependencies")
public abstract class DependenciesConfiguration extends AbstractConfiguration
{
    public abstract List<DependencyConfiguration> getDependencies();
}
