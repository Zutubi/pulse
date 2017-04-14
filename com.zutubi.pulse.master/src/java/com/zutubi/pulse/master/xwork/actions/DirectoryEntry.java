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

package com.zutubi.pulse.master.xwork.actions;

import com.zutubi.pulse.servercore.filesystem.File;

/**
 */
public class DirectoryEntry
{
    private String path;
    private boolean isDirectory;
    private String name;
    private String mimeType;
    private long size;

    public DirectoryEntry(File file, String name)
    {
        this.path = file.getPath();
        isDirectory = file.isDirectory();
        if (isDirectory)
        {
            mimeType = "directory";
        }
        else
        {
            mimeType = file.getMimeType();
        }
        this.name = name;
        size = file.length();
    }

    public String getPath()
    {
        return path;
    }

    public boolean isDirectory()
    {
        return isDirectory;
    }

    public String getName()
    {
        return name;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public long getSize()
    {
        return size;
    }

    public String getPrettySize()
    {
        double s;
        String units;

        if (size > 1024 * 1024)
        {
            s = size / (1024 * 1024.0);
            units = "MB";
        }
        else if (size > 1024)
        {
            s = size / 1024.0;
            units = "kB";
        }
        else
        {
            return size + " bytes";
        }

        return String.format("%.02f %s", s, units);
    }
}
