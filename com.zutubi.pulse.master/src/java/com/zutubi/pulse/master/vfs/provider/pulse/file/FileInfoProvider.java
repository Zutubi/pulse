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
