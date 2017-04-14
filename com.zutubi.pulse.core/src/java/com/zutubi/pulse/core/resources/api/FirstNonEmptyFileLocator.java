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
import java.util.Collections;

/**
 * A file locator that tries child locators in order and returns the results of
 * the first child to find any files.  If no child finds any files, the empty
 * list is returned.
 */
public class FirstNonEmptyFileLocator implements FileLocator
{
    private FileLocator[] delegates;

    /**
     * Creates a locator that will try the given delegates in order until one
     * returns a non-empty list.
     * 
     * @param delegates delegates to try, in order
     */
    public FirstNonEmptyFileLocator(FileLocator... delegates)
    {
        this.delegates = delegates;
    }

    public Collection<File> locate()
    {
        for (FileLocator delegate: delegates)
        {
            Collection<File> results = delegate.locate();
            if (results.size() > 0)
            {
                return results;
            }
        }
        
        return Collections.emptyList();
    }
}
