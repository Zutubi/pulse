package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.NamedConfiguration;

/**
 * Basic interface for output capture configurations.
 */
@SymbolicName("zutubi.outputConfig")
public interface OutputConfiguration extends NamedConfiguration
{
    Output createOutput();
}
