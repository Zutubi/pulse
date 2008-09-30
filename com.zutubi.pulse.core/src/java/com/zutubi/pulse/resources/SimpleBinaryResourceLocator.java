package com.zutubi.pulse.resources;

/**
 * Finds a resource by searching for a binary in the PATH.
 */
public class SimpleBinaryResourceLocator extends FileSystemResourceLocator
{
    public SimpleBinaryResourceLocator(String resourceName)
    {
        this(resourceName, resourceName);
    }

    public SimpleBinaryResourceLocator(String resourceName, String binaryName)
    {
        super(new BinaryInPathFileLocator(binaryName), new SimpleBinaryResourceBuilder(resourceName));
    }
}
