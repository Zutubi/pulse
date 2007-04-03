package com.zutubi.pulse.filesystem.remote;

import com.zutubi.pulse.scm.SCMFile;

import java.util.LinkedList;
import java.util.List;

/**
 * A remote file that caches a list of child files.
 */
public class CachingSCMFile extends SCMFile
{
    private List<SCMFile> children = new LinkedList<SCMFile>();

    public CachingSCMFile(String name, boolean isDirectory, SCMFile parent, String path)
    {
        super(name, isDirectory, parent, path);
    }

    public CachingSCMFile(boolean isDirectory, SCMFile parent, String path)
    {
        super(isDirectory, parent, path);
    }

    public void addChild(SCMFile child)
    {
        children.add(child);
    }

    public List<SCMFile> list()
    {
        return children;
    }
}
