package com.zutubi.pulse.core.engine;

import com.zutubi.tove.config.api.NamedConfiguration;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.pulse.core.Command;

/**
 */
@SymbolicName("zutubi.commandConfig")
public interface CommandConfiguration extends NamedConfiguration
{
    Command createCommand();
}
