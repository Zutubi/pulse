package com.zutubi.pulse.core.resources;

import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.util.FileSystemUtils;

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

    public ResourceConfiguration buildResource(File file)
    {
        ResourceConfiguration resource = new ResourceConfiguration(resourceName);
        resource.addProperty(new ResourcePropertyConfiguration(resourceName + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_BINARY, FileSystemUtils.normaliseSeparators(file.getAbsolutePath()), false, false, false));

        File binaryDir = file.getParentFile();
        if (binaryDir != null)
        {
            resource.addProperty(new ResourcePropertyConfiguration(resourceName + PROPERTY_SEPARATOR + PROPERTY_SUFFIX_BINARY_DIRECTORY, FileSystemUtils.normaliseSeparators(binaryDir.getAbsolutePath()), false, false, false));
        }
        return resource;
    }
}
