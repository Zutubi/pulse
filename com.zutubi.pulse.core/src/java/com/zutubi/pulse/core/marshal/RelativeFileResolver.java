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

package com.zutubi.pulse.core.marshal;

import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.io.FileSystemUtils;

import java.io.InputStream;

/**
 * Resolves files relative to another path (unless, of course, the path is
 * absolute).
 */
public class RelativeFileResolver implements FileResolver
{
    private String basePath;
    private FileResolver delegate;

    public RelativeFileResolver(String filePath, FileResolver delegate)
    {
        if (filePath != null)
        {
            this.basePath = FileSystemUtils.appendAndCanonicalise(null, PathUtils.getParentPath(filePath));
        }
        this.delegate = delegate;
    }

    public InputStream resolve(String path) throws Exception
    {
        return delegate.resolve(FileSystemUtils.appendAndCanonicalise(basePath, path));
    }
}
