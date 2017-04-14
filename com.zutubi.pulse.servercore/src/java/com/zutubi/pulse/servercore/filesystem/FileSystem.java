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

import java.io.InputStream;

/**
 * The FileSystem provides a layer of abstraction on top of a resource
 * that provides access to files.
 *
 * For example: a file system could be the local file system or it could
 * be a branch on a remote scm repository.
 */
public interface FileSystem
{
    InputStream getFileContents(String path) throws FileSystemException;

    InputStream getFileContents(File file) throws FileSystemException;

    /**
     * Retrieve the file identified by the path.
     *
     * @param path
     *
     * @return a file instance.
     *
     * @throws FileNotFoundException if the file referenced by the path does
     * not exist.
     */
    File getFile(String path) throws FileSystemException;

    /**
     * Retrieve the mime type of the file defined by the specified path.
     *
     * @param filePath specifying the file whose mime type we are retrieving.
     *
     * @return the mime type of the specified file, or null if it can not be
     * determined.
     *
     * @throws FileSystemException if there is a problem determining the files
     * mime type.
     */
    String getMimeType(String filePath) throws FileSystemException;

    String getMimeType(File file) throws FileNotFoundException;

    /**
     * Retrieve the list of files that are children of the specified path.
     *
     * @param dirPath
     *
     * @return an array of files, or an empty array if the dirPath has no children.
     *
     * @throws FileSystemException if the dirPath does not refer to a directory.
     * @throws FileNotFoundException if the dirPath is not a valid path.
     */
    File[] list(String dirPath) throws FileSystemException;

    File[] list(File dir) throws FileSystemException;

    String getSeparator();
}
