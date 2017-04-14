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

import java.io.File;
import java.util.Collection;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;

/**
 * A file locator takes the output of one locator and returns all non-null
 * parent directories of that output.
 */
public class ParentsFileLocator implements FileLocator
{
    private FileLocator delegate;

    /**
     * Creates a locator that will find parents of the given locator's output.
     * 
     * @param delegate delegate used to find initial output
     */
    public ParentsFileLocator(FileLocator delegate)
    {
        this.delegate = delegate;
    }

    public Collection<File> locate()
    {
        return newArrayList(filter(transform(delegate.locate(), new Function<File, File>()
        {
            public File apply(File file)
            {
                return file.getParentFile();
            }
        }), Predicates.notNull()));
    }
}
