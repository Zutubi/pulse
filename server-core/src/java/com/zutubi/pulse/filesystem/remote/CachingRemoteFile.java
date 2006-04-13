/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.filesystem.remote;

import java.util.LinkedList;
import java.util.List;

/**
 * A remote file that caches a list of child files.
 */
public class CachingRemoteFile extends RemoteFile
{
    private List<RemoteFile> children = new LinkedList<RemoteFile>();

    public CachingRemoteFile(String name, boolean isDirectory, RemoteFile parent, String path)
    {
        super(name, isDirectory, parent, path);
    }

    public CachingRemoteFile(boolean isDirectory, RemoteFile parent, String path)
    {
        super(isDirectory, parent, path);
    }

    public void addChild(RemoteFile child)
    {
        children.add(child);
    }

    public List<RemoteFile> list()
    {
        return children;
    }
}
