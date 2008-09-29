package com.zutubi.pulse.resources;

import java.io.File;
import java.util.List;

/**
 */
public class HomeDirectoryFileLocator implements FileLocator
{
    private FileLocator delegate;

    public HomeDirectoryFileLocator(String environmentVariable)
    {
        delegate = new DirectoryFilteringFileLocator(new EnvironmentVariableFileLocator(environmentVariable));
    }

    public List<File> locate()
    {
        return delegate.locate();
    }
}
