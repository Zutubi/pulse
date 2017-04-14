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
 * An absraction of the files used by the FileSystem.
 *
 */
public interface File
{
    /**
     * Indicates whether or not this file represents a directory.
     *
     * @return true if this file represents a directory in the filesystem, false otherwise.
     */
    boolean isDirectory();

    /**
     * Indicates whether or not this file represents a file.
     *
     * @return true if this file represents a file in the filesystem, false otherwise.
     */
    boolean isFile();

    /**
     * Retrieve the parent of this file. The root file of a filesystem will not have a
     * parent.
     *
     * @return this files parent (a directory) or null if this file has no parent.
     */
    File getParentFile();

    String getMimeType();

    long length();

    String getName();

    String getPath();

    String getAbsolutePath();
}
