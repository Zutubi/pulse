package com.cinnamonbob.filesystem;

import java.io.InputStream;

/**
 * <class-comment/>
 */
public interface FileSystem
{
    InputStream getFileContents(String path) throws FileSystemException;

    InputStream getFileContents(File file) throws FileSystemException;

    File getFile(String path) throws FileSystemException;

    String getMimeType(String path) throws FileSystemException;

    String getMimeType(File file) throws FileNotFoundException;

    File[] list(String path);

    File[] list(File dir);

    String getSeparator();
}
