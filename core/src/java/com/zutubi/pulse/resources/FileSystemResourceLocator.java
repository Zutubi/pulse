package com.zutubi.pulse.resources;

import com.zutubi.pulse.core.config.Resource;
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

    public List<Resource> locate()
    {
        List<Resource> resources = CollectionUtils.map(fileLocator.locate(), new Mapping<File, Resource>()
        {
            public Resource map(File file)
            {
                return resourceBuilder.buildResource(file);
            }
        });

        resources = CollectionUtils.filter(resources, new Predicate<Resource>()
        {
            public boolean satisfied(Resource resource)
            {
                return resource != null;
            }
        });
        
        return resources;
    }
}
