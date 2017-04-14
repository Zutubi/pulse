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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * Wrap FileSystemUtils in an object so it can be injected for testing.
 */
public class FileSystem
{
    /**
     * Tests if a file with the given name exists, based on
     * {@link java.io.File#exists()}.
     *
     * @param fileName the file name to test
     * @return true iff the given file exists
     * @see java.io.File#exists()
     */
    public boolean exists(String fileName)
    {
        return new File(fileName).exists();
    }

    /**
     * Tests if a file with the given name exists and is a directory, based on
     * {@link java.io.File#isDirectory()}.
     *
     * @param fileName the file name to test
     * @return true iff the given file exists and is a directory
     * @see java.io.File#isDirectory()
     */
    public boolean isDirectory(String fileName)
    {
        return new File(fileName).exists();
    }
    
    /**
     * Wrapper around {@link File#listRoots()} which ensures that null is never
     * returned.
     *
     * @return an array of file system roots, which will be empty if there are
     *         none or they cannot be determined
     * @see java.io.File#listRoots()
     */
    public File[] listRoots()
    {
        return convertNullToEmpty(File.listRoots());
    }

    /**
     * Wrapper around {@link File#listFiles(java.io.FilenameFilter)} which
     * ensures that null is never returned.
     *
     * @param dir    directory on which listFiles is called
     * @param filter filter passed to listFiles
     * @return an array of matching files, may be empty
     * @see File#listFiles(java.io.FilenameFilter)
     */
    public File[] listFiles(File dir, FilenameFilter filter)
    {
        return convertNullToEmpty(dir.listFiles(filter));
    }

    /**
     * Deletes the given path, recursively if it is a directory.
     * 
     * @param path the path to delete
     * @throws IOException if the path cannot be fully deleted
     */
    public void delete(File path) throws IOException
    {
        FileSystemUtils.delete(path);
    }

    private File[] convertNullToEmpty(File[] files)
    {
        if (files == null)
        {
            return new File[0];
        }

        return files;
    }
}
