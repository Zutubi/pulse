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

package com.zutubi.pulse.core.scm.api;

import com.google.common.base.Predicate;
import com.zutubi.util.io.FileSystemUtils;

/**
 * A predicate that rejects all path strings that do not have the
 * specified path prefix.
 *
 * All strings are normalised to allow separator chars to be handled
 * predictably.
 */
public class PrefixPathFilter implements Predicate<String>
{
    /**
     * The required prefix of all accepted paths.
     */
    private String prefix;

    public PrefixPathFilter(String prefix)
    {
        this.prefix = normalisePath(prefix);
    }

    public boolean apply(String path)
    {
        String normalisedPath = normalisePath(path);
        return normalisedPath.startsWith(prefix);
    }

    private String normalisePath(String path)
    {
        return FileSystemUtils.localiseSeparators(path);
    }
}
