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

import java.io.File;
import java.util.Collection;

/**
 * A file locator that locates a directory based on the value of an environment
 * variable.  If the variable does not exist or does not point to a directory,
 * nothing is returned.
 */
public class EnvironmentVariableDirectoryLocator implements FileLocator
{
    private FileLocator delegate;

    /**
     * Creates a locator that will look for directories specified by the given
     * variable.
     * 
     * @param environmentVariable name of the variable to look for
     */
    public EnvironmentVariableDirectoryLocator(String environmentVariable)
    {
        delegate = new DirectoryFilteringFileLocator(new EnvironmentVariableFileLocator(environmentVariable));
    }

    public Collection<File> locate()
    {
        return delegate.locate();
    }
}
