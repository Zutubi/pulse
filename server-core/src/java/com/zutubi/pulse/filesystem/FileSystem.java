package com.zutubi.pulse.filesystem;

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

    void close();
}
