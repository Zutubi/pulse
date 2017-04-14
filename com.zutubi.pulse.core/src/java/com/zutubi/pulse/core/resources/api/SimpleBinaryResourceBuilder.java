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

import com.zutubi.util.io.FileSystemUtils;

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
     *
     * @param resourceName name of the resource and binary property prefix
     */
    public SimpleBinaryResourceBuilder(String resourceName)
    {
        this.resourceName = resourceName;
    }

    public ResourceConfiguration buildResource(File path)
    {
        ResourceConfiguration resource = new ResourceConfiguration(resourceName);
        resource.addProperty(new ResourcePropertyConfiguration(resourceName + PROPERTY_SUFFIX_BINARY, FileSystemUtils.normaliseSeparators(path.getAbsolutePath()), false, false));

        File binaryDir = path.getParentFile();
        if (binaryDir != null)
        {
            resource.addProperty(new ResourcePropertyConfiguration(resourceName + PROPERTY_SUFFIX_BINARY_DIRECTORY, FileSystemUtils.normaliseSeparators(binaryDir.getAbsolutePath()), false, false));
        }
        return resource;
    }
}
