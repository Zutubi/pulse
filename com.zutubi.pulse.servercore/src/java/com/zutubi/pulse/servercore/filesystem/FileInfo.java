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

package com.zutubi.pulse.servercore.filesystem;

/**
 * The FileInfo is a value object used to hold information file
 * related information for serialisation via hessian.
 */
public class FileInfo
{
    private boolean exists;
    private boolean hidden;
    private boolean directory;
    private boolean file;
    private String name;
    private long length;

    public FileInfo()
    {
        // for hessian.
    }

    public FileInfo(java.io.File f)
    {
        exists = f.exists();
        hidden = f.isHidden();
        file = f.isFile();
        directory = f.isDirectory();
        name = f.getName();
        length = f.length();
    }

    public boolean exists()
    {
        return exists;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public boolean isFile()
    {
        return file;
    }

    public String getName()
    {
        return name;
    }

    public long length()
    {
        return length;
    }
}
