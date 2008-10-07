package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.ScmFile;

import java.util.LinkedList;
import java.util.List;

/**
 * A remote file that caches a list of child files.
 */
public class CachingScmFile extends ScmFile
{
    private List<ScmFile> children = new LinkedList<ScmFile>();

    public CachingScmFile(String path, boolean isDirectory)
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
