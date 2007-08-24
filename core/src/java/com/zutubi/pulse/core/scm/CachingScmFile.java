package com.zutubi.pulse.core.scm;

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
        super(path, name, isDirectory);
    }

    public CachingScmFile(boolean isDirectory, ScmFile parent, String path)
    {
        super(path, isDirectory);
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
