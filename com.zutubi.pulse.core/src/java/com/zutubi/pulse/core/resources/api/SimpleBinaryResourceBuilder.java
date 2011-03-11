package com.zutubi.pulse.core.resources.api;

import com.zutubi.util.FileSystemUtils;

import java.io.File;

/**
 * A resource builder that takes the path of a binary file and creates a
 * standard simple binary resource from it.  Such resources have two
 * properties:
 * 
 * <ol>
 *     <li>&lt;name&gt;.bin - pointing to the binary file</li>
 *     <li>&lt;name&gt;.bin.dir - pointing to the directory containing the binary</li>
 * </ol>
 */
public class SimpleBinaryResourceBuilder implements FileSystemResourceBuilder
{
    private String resourceName;

    /**
     * Creates a builder that build resources with the given name.
     * @param resourceName
     */
    public SimpleBinaryResourceBuilder(String resourceName)
    {
        this.resourceName = resourceName;
    }

    public ResourceConfiguration buildResource(File path)
    {
        ResourceConfiguration resource = new ResourceConfiguration(resourceName);
        resource.addProperty(new ResourcePropertyConfiguration(resourceName + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_BINARY, FileSystemUtils.normaliseSeparators(path.getAbsolutePath()), false, false, false));

        File binaryDir = path.getParentFile();
        if (binaryDir != null)
        {
            resource.addProperty(new ResourcePropertyConfiguration(resourceName + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_BINARY_DIRECTORY, FileSystemUtils.normaliseSeparators(binaryDir.getAbsolutePath()), false, false, false));
        }
        return resource;
    }
}
