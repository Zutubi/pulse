/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
