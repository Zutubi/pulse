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
