package com.zutubi.pulse.core.commands.maven2;

import com.zutubi.pulse.core.resources.api.StandardHomeDirectoryResourceLocator;

/**
 * Locates a maven 2 installation using the MAVEN2_HOME environment variable.
 */
public class Maven2ResourceLocator extends StandardHomeDirectoryResourceLocator
{
    public Maven2ResourceLocator()
    {
        super("maven2", "MAVEN2_HOME", "mvn", true);
    }
}
