package com.cinnamonbob.filesystem;

import com.cinnamonbob.filesystem.local.LocalFile;

/**
 * <class-comment/>
 */
public interface File
{
    boolean isDirectory();

    boolean isFile();

    LocalFile getParentFile();

    String getMimeType();

    long length();

    String getName();
}
