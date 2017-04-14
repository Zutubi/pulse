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

import static com.zutubi.pulse.core.resources.api.StandardHomeDirectoryConstants.convertResourceNameToEnvironmentVariable;

import java.util.List;

/**
 * A resource locator that finds resource which match a common template.  Such
 * resources are tools located at a path specified by a resource variable.  The
 * path contains at least a bin subdirectory with a binary of a given name.
 * <p/>
 * For example, apache ant is often found at a path specified by ANT_HOME, and
 * an ant installation directory contains a bin subdirectory within which the
 * ant script may be found.
 * 
 * @see StandardHomeDirectoryFileLocator
 * @see StandardHomeDirectoryResourceBuilder
 */
public class StandardHomeDirectoryResourceLocator implements ResourceLocator
{
    private ResourceLocator delegate;

    /**
     * Creates a simple locator where the environment variable and binary names
     * are deducible from the resource name.  The environment variable name is
     * created by uppercasing the resource name and appending _HOME.  The
     * script name is identical to the resource name.
     * 
     * @param resourceName name of the resource to create, implies the name of
     *                     the environment variable and binary 
     * @param script       if true, the binary is a script, otherwise it is an
     *                     actual binary (used to determine the suffix on
     *                     Windows)
     */
    public StandardHomeDirectoryResourceLocator(String resourceName, boolean script)
    {
        this(resourceName, convertResourceNameToEnvironmentVariable(resourceName), resourceName, script);
    }

    /**
     * Creates a locator that will build resources of the given name by looking
     * for the given environment variable and binary.
     * 
     * @param resourceName        name of the resource to create
     * @param environmentVariable name of the variable that points to the home
     *                            directory
     * @param binaryName          name of the tool binary
     * @param script              if true, the binary is a script, otherwise it
     *                            is an actual binary (used to determine the
     *                            suffix on Windows)
     */
    public StandardHomeDirectoryResourceLocator(String resourceName, String environmentVariable, String binaryName, boolean script)
    {
        delegate = new FileSystemResourceLocator(new StandardHomeDirectoryFileLocator(environmentVariable, binaryName, script), new StandardHomeDirectoryResourceBuilder(resourceName, binaryName, script));
    }

    public List<ResourceConfiguration> locate()
    {
        return delegate.locate();
    }
}
