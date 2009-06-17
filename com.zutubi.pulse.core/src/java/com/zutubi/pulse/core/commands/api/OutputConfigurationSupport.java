package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;

/**
 * Helper base class for configuration of {@link Output} instances.
 */
@SymbolicName("zutubi.outputConfigSupport")
public abstract class OutputConfigurationSupport extends AbstractNamedConfiguration implements OutputConfiguration
{
    protected OutputConfigurationSupport()
    {
    }

    protected OutputConfigurationSupport(String name)
    {
        super(name);
    }
}
