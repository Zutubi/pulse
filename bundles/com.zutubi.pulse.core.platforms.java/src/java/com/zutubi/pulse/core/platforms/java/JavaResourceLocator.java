package com.zutubi.pulse.core.platforms.java;

import com.zutubi.pulse.core.resources.api.StandardHomeDirectoryResourceLocator;

/**
 * Locates a Java installation using JAVA_HOME.
 */
public class JavaResourceLocator extends StandardHomeDirectoryResourceLocator
{
    public JavaResourceLocator()
    {
        super("java", true);
    }
}
