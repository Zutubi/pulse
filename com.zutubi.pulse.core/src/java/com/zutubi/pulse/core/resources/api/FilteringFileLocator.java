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
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Collection;

/**
 * A file locator that takes files located by a child locator and filters out
 * any that do not match a given predicate.
 */
public class FilteringFileLocator implements FileLocator
{
    private FileLocator delegate;
    private Predicate<File> predicate;

    /**
     * Creates a new locator that finds candidates with the given delegate and
     * filters them with the given predicate.
     * 
     * @param delegate  child locator used to find candidate files
     * @param predicate predicate used to filter candidates - only files
     *                  satisfying the predicate are returned from
     *                  {@link #locate()}.
     */
    public FilteringFileLocator(FileLocator delegate, Predicate<File> predicate)
    {
        this.delegate = delegate;
        this.predicate = predicate;
    }

    public Collection<File> locate()
    {
        return Lists.newArrayList(Collections2.filter(delegate.locate(), predicate));
    }
}
