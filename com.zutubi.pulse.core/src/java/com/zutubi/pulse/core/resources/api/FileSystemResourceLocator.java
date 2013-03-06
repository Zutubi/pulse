package com.zutubi.pulse.core.resources.api;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import java.io.File;
import java.util.List;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newLinkedList;

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
        List<ResourceConfiguration> resources = newLinkedList(transform(fileLocator.locate(), new Function<File, ResourceConfiguration>()
        {
            public ResourceConfiguration apply(File file)
            {
                return resourceBuilder.buildResource(file);
            }
        }));

        Iterables.removeIf(resources, Predicates.isNull());
        return resources;
    }
}
