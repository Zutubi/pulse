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

import com.google.common.base.Predicate;

import java.io.File;

/**
 * A file locator that takes candidates from a delegate locator and filters out
 * any that are not plain files.  Thus {@link #locate()} only returns paths
 * that point to existing plain files.
 */
public class PlainFileFilteringFileLocator extends FilteringFileLocator
{
    /**
     * Creates a plain file locator with the given delegate.
     * 
     * @param delegate locator used to find candidate paths
     */
    public PlainFileFilteringFileLocator(FileLocator delegate)
    {
        super(delegate, new Predicate<File>()
        {
            public boolean apply(File file)
            {
                return file.isFile();
            }
        });
    }
}
