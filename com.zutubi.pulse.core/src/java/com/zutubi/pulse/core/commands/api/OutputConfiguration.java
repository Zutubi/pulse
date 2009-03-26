package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.NamedConfiguration;

/**
 * Basic interface for output capture configurations.
 */
@SymbolicName("zutubi.outputConfig")
public interface OutputConfiguration extends NamedConfiguration
{
    /**
     * Indicates the type of output to create for this configuration.  This
     * output type must have single-argument constructor which will accept
     * this configuration.
     *
     * @return the type of output to create for this configuration
     */
    Class<? extends Output> outputType();
}
