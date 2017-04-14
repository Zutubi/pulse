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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * A resolver that finds files on the local file system.
 */
public class LocalFileResolver implements FileResolver
{
    private File root;

    public LocalFileResolver(File root)
    {
        this.root = root;
    }

    public InputStream resolve(String path) throws Exception
    {
        if (path.startsWith(PathUtils.SEPARATOR))
        {
            path = path.substring(PathUtils.SEPARATOR.length());
        }

        return new FileInputStream(new File(root, path));
    }
}
