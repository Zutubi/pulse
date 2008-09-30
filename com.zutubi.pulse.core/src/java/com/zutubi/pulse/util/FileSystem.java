package com.zutubi.pulse.util;

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

    public boolean rmdir(File dir)
    {
        return FileSystemUtils.rmdir(dir);
    }

    public void delete(File file) throws IOException
    {
        FileSystemUtils.delete(file);
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
