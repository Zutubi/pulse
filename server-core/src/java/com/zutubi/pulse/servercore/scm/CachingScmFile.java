package com.zutubi.pulse.servercore.scm;

import com.zutubi.pulse.servercore.scm.ScmFile;

import java.util.LinkedList;
import java.util.List;

/**
 * A remote file that caches a list of child files.
 */
public class CachingScmFile extends ScmFile
{
    private List<ScmFile> children = new LinkedList<ScmFile>();

    public CachingScmFile(String name, boolean isDirectory, ScmFile parent, String path)
    {
        super(name, isDirectory, parent, path);
    }

    public CachingScmFile(boolean isDirectory, ScmFile parent, String path)
    {
        super(isDirectory, parent, path);
    }

    public void addChild(ScmFile child)
    {
        children.add(child);
    }

    public List<ScmFile> list()
    {
        return children;
    }
}
