package com.zutubi.pulse.core.resources;

import com.zutubi.pulse.core.config.Resource;
import static com.zutubi.pulse.core.resources.StandardHomeDirectoryConstants.convertResourceNameToEnvironmentVariable;

import java.util.List;

/**
 */
public class StandardHomeDirectoryResourceLocator implements ResourceLocator
{
    private ResourceLocator delegate;

    public StandardHomeDirectoryResourceLocator(String resourceName, boolean script)
    {
        this(resourceName, convertResourceNameToEnvironmentVariable(resourceName), resourceName, script);
    }

    public StandardHomeDirectoryResourceLocator(String resourceName, String environmentVariable, String binaryName, boolean script)
    {
        delegate = new FileSystemResourceLocator(new StandardHomeDirectoryFileLocator(environmentVariable, binaryName, script), new StandardHomeDirectoryResourceBuilder(resourceName, binaryName, script));
    }

    public List<Resource> locate()
    {
        return delegate.locate();
    }
}
