package com.zutubi.pulse.core.commands;

import com.zutubi.pulse.core.ConfiguredInstanceFactory;
import com.zutubi.pulse.core.commands.api.Output;
import com.zutubi.pulse.core.commands.api.OutputConfiguration;

/**
 * Default implementation of {@link OutputFactory}, which uses the object
 * factory to build outputs.
 */
public class DefaultOutputFactory extends ConfiguredInstanceFactory<Output, OutputConfiguration> implements OutputFactory
{
    protected Class<? extends Output> getType(OutputConfiguration configuration)
    {
        return configuration.outputType();
    }
}