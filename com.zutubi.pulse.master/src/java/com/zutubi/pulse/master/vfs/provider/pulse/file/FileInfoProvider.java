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

package com.zutubi.pulse.master.vfs.provider.pulse.file;

import com.zutubi.pulse.servercore.filesystem.FileInfo;

import java.util.List;

/**
 * A provider interface that gives access to FileInfo instances.  The class
 * implementing this interface is treated as the base to which all paths
 * are relative to.
 */
public interface FileInfoProvider
{
    /**
     * Get a listing of the files at the specified path.
     * @param path  the path relative to the provider node.
     *
     * @return a list of file info instances representing the files at the
     * specified path.
     *
     * @throws Exception on error
     */
    List<FileInfo> getFileInfos(String path) throws Exception;

    /**
     * Get the file info for a specific file.
     *
     * @param path  the path relative to the provider node that identifies
     *              the requested file info.
     * @return the file info for the specified path.
     *
     * @throws Exception on error
     */
    FileInfo getFileInfo(String path) throws Exception;
}
