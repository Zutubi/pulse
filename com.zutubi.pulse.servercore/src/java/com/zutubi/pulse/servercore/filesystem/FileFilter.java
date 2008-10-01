package com.zutubi.pulse.servercore.filesystem;

/**
 *
 *
 */
public interface FileFilter
{
    boolean accept(File pathname);
}
