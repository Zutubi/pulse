package com.cinnamonbob.filesystem.cvs;

import com.cinnamonbob.filesystem.File;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
public class RemoteCvsFile implements File, Comparable
{
    private String name;
    private String path;
    private boolean isDir;
    private RemoteCvsFile parent;
    private List<RemoteCvsFile> children = new LinkedList<RemoteCvsFile>();

    public RemoteCvsFile(String name, boolean isDirectory, RemoteCvsFile parent, String path)
    {
        this.name = name;
        this.isDir = isDirectory;
        this.parent = parent;
        this.path = path;
    }

    public boolean isDirectory()
    {
        return isDir;
    }

    public boolean isFile()
    {
        return !isDirectory();
    }

    public File getParentFile()
    {
        return parent;
    }

    public String getMimeType()
    {
        return "text/plain";
    }

    public long length()
    {
        return 0;
    }

    public String getName()
    {
        return name;
    }

    public String getPath()
    {
        return path;
    }

    protected void addChild(RemoteCvsFile child)
    {
        children.add(child);
    }

    protected List<RemoteCvsFile> list()
    {
        return children;
    }

    public int compareTo(Object o)
    {
        RemoteCvsFile other = (RemoteCvsFile) o;
        return name.compareTo(other.name);
    }
}
