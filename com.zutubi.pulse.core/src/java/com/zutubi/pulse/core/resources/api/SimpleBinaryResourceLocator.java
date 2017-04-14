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

/**
 * Finds a resource by searching for a binary in the PATH.  This locator can be
 * used to create resources that have a single &lt;name&gt;.bin property
 * pointing to a binary on the search PATH.  By convention, the resource and
 * property are named after the binary itself, e.g. a resource named "make" is
 * created with a single property "make.bin" for the executable "make". You
 * may, however, choose a different name for the resource and property.
 */
public class SimpleBinaryResourceLocator extends FileSystemResourceLocator
{
    /**
     * Creates a locator that uses the convention of naming the property after
     * the binary.
     * 
     * @param resourceName name of the binary to find (and resource to create)
     */
    public SimpleBinaryResourceLocator(String resourceName)
    {
        this(resourceName, resourceName);
    }

    /**
     * Creates a locator that uses a custom resource name.
     * 
     * @param resourceName name to give to the located resources (and prefix
     *                     for the .bin property)
     * @param binaryName   name of the binary to search for
     */
    public SimpleBinaryResourceLocator(String resourceName, String binaryName)
    {
        super(new BinaryInPathFileLocator(binaryName), new SimpleBinaryResourceBuilder(resourceName));
    }
}
