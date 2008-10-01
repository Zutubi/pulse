package com.zutubi.pulse.resources;

import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceProperty;
import com.zutubi.pulse.core.util.FileSystemUtils;

import java.io.File;

/**
 */
public class SimpleBinaryResourceBuilder implements ResourceBuilder
{
    private String resourceName;

    public SimpleBinaryResourceBuilder(String resourceName)
    {
        this.resourceName = resourceName;
    }

    public Resource buildResource(File file)
    {
        Resource resource = new Resource(resourceName);
        resource.addProperty(new ResourceProperty(resourceName + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_BINARY, FileSystemUtils.normaliseSeparators(file.getAbsolutePath()), false, false, false));

        File binaryDir = file.getParentFile();
        if (binaryDir != null)
        {
            resource.addProperty(new ResourceProperty(resourceName + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_BINARY_DIRECTORY, FileSystemUtils.normaliseSeparators(binaryDir.getAbsolutePath()), false, false, false));
        }
        return resource;
    }
}
