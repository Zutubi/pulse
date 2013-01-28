package com.zutubi.pulse.core.resources.api;

import java.io.File;
import java.util.Collection;

/**
 * A file locator that locates a directory based on the value of an environment
 * variable.  If the variable does not exist or does not point to a directory,
 * nothing is returned.
 */
public class EnvironmentVariableDirectoryLocator implements FileLocator
{
    private FileLocator delegate;

    /**
     * Creates a locator that will look for directories specified by the given
     * variable.
     * 
     * @param environmentVariable name of the variable to look for
     */
    public EnvironmentVariableDirectoryLocator(String environmentVariable)
    {
        delegate = new DirectoryFilteringFileLocator(new EnvironmentVariableFileLocator(environmentVariable));
    }

    public Collection<File> locate()
    {
        return delegate.locate();
    }
}
