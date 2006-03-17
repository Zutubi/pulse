package com.cinnamonbob.filesystem;

/**
 * <class-comment/>
 */
public interface File
{
    boolean isDirectory();

    boolean isFile();

    File getParentFile();

    String getMimeType();

    long length();

    String getName();

    String getPath();
}
