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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * A file locator that sorts the results of a delegate locator using a
 * comparator.  This is useful if the files should be considered in a specific
 * order.
 */
public class SortingFileLocator implements FileLocator
{
    private FileLocator delegate;
    private Comparator<? super File> comparator;

    /**
     * Creates a locator that will sort the results of the given delegate using
     * the given comparator.
     * 
     * @param delegate   child locator to generate raw results
     * @param comparator comparator used to sort the results
     */
    public SortingFileLocator(FileLocator delegate, Comparator<? super File> comparator)
    {
        this.delegate = delegate;
        this.comparator = comparator;
    }

    public List<File> locate()
    {
        List<File> result = new LinkedList<File>(delegate.locate());
        Collections.sort(result, comparator);
        return result;
    }
}
