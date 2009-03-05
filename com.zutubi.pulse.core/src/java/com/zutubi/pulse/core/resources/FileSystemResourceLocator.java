package com.zutubi.pulse.core.resources;

import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.List;

/**
 */
public class FileSystemResourceLocator implements ResourceLocator
{
    private FileLocator fileLocator;
    private ResourceBuilder resourceBuilder;

    public FileSystemResourceLocator(FileLocator fileLocator, ResourceBuilder resourceBuilder)
    {
        this.fileLocator = fileLocator;
        this.resourceBuilder = resourceBuilder;
    }

    public List<ResourceConfiguration> locate()
    {
        List<ResourceConfiguration> resources = CollectionUtils.map(fileLocator.locate(), new Mapping<File, ResourceConfiguration>()
        {
            public ResourceConfiguration map(File file)
            {
                return resourceBuilder.buildResource(file);
            }
        });

        resources = CollectionUtils.filter(resources, new Predicate<ResourceConfiguration>()
        {
            public boolean satisfied(ResourceConfiguration resource)
            {
                return resource != null;
            }
        });
        
        return resources;
    }
}
