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

package com.zutubi.pulse.servercore.agent;

import com.zutubi.util.io.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * A synchronisation task that renames a directory.  If the source does not
 * exist, the request is ignored (this should also catch retries).  If the
 * destination already exists, the task fails.
 */
public class RenameDirectoryTask implements SynchronisationTask
{
    private String source;
    private String dest;

    public RenameDirectoryTask()
    {
    }

    /**
     * Create a new task to rename source to dest.
     *
     * @param source path of the source file, to be renamed
     * @param dest   the new path to rename to
     */
    public RenameDirectoryTask(String source, String dest)
    {
        this.source = source;
        this.dest = dest;
    }

    public void execute()
    {
        File sourceFile = new File(source);
        if (sourceFile.exists())
        {
            try
            {
                File destFile = new File(dest);
                if (destFile.exists())
                {
                    throw new RuntimeException("Cannot rename '" + source + "': destination '" + dest + "' already exists");
                }

                FileSystemUtils.robustRename(sourceFile, destFile);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}