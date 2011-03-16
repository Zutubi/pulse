package com.zutubi.pulse.core.commands.ant;

import com.zutubi.pulse.core.resources.api.StandardHomeDirectoryResourceLocator;

/**
 * Locates an ant installation using ANT_HOME.
 */
public class AntResourceLocator extends StandardHomeDirectoryResourceLocator
{
    public AntResourceLocator()
    {
        super("ant", true);
    }
}
