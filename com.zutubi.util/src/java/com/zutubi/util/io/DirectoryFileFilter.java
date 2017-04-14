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

package com.zutubi.util.io;

import java.io.FileFilter;
import java.io.File;

/**
 * An implementation of the FileFilter interface that accepts only
 * directories.
 */
public class DirectoryFileFilter implements FileFilter
{
    public static final FileFilter INSTANCE = new DirectoryFileFilter();
    
    public boolean accept(File file)
    {
        return file.isDirectory();
    }
}
