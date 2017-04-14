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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A file locator that reverses the results of another locator.  This can be
 * useful to consider candidate paths in a different order.
 */
public class ReversingFileLocator implements FileLocator
{
    private FileLocator delegate;

    /**
     * Creates a locator that will reverse the results of the given delegate.
     * 
     * @param delegate delegate used to locate files
     */
    public ReversingFileLocator(FileLocator delegate)
    {
        this.delegate = delegate;
    }

    public List<File> locate()
    {
        List<File> result = new LinkedList<File>(delegate.locate());
        Collections.reverse(result);
        return result;
    }
}
