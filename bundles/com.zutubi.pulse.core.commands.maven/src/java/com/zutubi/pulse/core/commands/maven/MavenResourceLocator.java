package com.zutubi.pulse.core.commands.maven;

import com.zutubi.pulse.core.resources.api.StandardHomeDirectoryResourceLocator;

/**
 * Locates maven 1 using the MAVEN_HOME environment variable.
 */
public class MavenResourceLocator extends StandardHomeDirectoryResourceLocator
{
    public MavenResourceLocator()
    {
        super("maven", true);
    }
}
