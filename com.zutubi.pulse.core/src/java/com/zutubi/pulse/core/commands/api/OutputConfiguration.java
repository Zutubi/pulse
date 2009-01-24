package com.zutubi.pulse.core.commands.api;

import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.config.api.NamedConfiguration;

/**
 */
@SymbolicName("zutubi.outputConfig")
public interface OutputConfiguration extends NamedConfiguration
{
    Output createOutput();
}
