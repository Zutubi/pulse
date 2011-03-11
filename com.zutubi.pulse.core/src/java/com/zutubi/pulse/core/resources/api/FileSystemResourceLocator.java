package com.zutubi.pulse.core.resources.api;

import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;
import com.zutubi.util.Predicate;

import java.io.File;
import java.util.List;

/**
 * A resource locator that locates the resource by searching the file system
 * with a {@link FileLocator} and then creates resources using a
 * {@link FileSystemResourceBuilder}.
 * <p/>
 * The file locator is run to locate paths potentially containing resources,
 * then those paths are each passed to the builder to create the actual
 * resource.
 */
public class FileSystemResourceLocator implements ResourceLocator
{
    private FileLocator fileLocator;
    private FileSystemResourceBuilder resourceBuilder;

    /**
     * Creates a new locator that will search with the given locator and build
     * resources with the given builder.
     * 
     * @param fileLocator     locator used to find candidate paths
     * @param resourceBuilder builder used to convert paths into resources
     */
    public FileSystemResourceLocator(FileLocator fileLocator, FileSystemResourceBuilder resourceBuilder)
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
