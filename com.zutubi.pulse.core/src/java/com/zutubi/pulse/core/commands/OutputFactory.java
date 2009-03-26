package com.zutubi.pulse.core.commands;

import com.zutubi.pulse.core.commands.api.Output;
import com.zutubi.pulse.core.commands.api.OutputConfiguration;

/**
 * Factory for creating outputs from configuration.
 */
public interface OutputFactory
{
    /**
     * Create a new output from the given configuration.  The configuration
     * identifies the type of output to create, and that type should have a
     * single-parameter constructor which will accept the configuration as an
     * argument.
     *
     * @param configuration configuration used to build the output
     * @return the created output
     * @throws com.zutubi.pulse.core.engine.api.BuildException on any error
     */
    Output create(OutputConfiguration configuration);
}