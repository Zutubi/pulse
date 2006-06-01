package com.zutubi.pulse.filesystem.remote;

import com.zutubi.pulse.filesystem.File;

/**
 * <class-comment/>
 */
public class RemoteFile implements File, Comparable
{
    private String name;
    private String path;
    private boolean isDir;
    private RemoteFile parent;
    private String type = "text/plain";

    public RemoteFile(String name, boolean isDirectory, RemoteFile parent, String path)
    {
        this.name = name;
        this.isDir = isDirectory;
        this.parent = parent;

        if (path.endsWith("/"))
        {
            path = path.substring(0, path.length() - 1);
        }
        this.path = path;
    }

    public RemoteFile(boolean isDirectory, RemoteFile parent, String path)
    {
        this(null, isDirectory, parent, path);

        int index = path.lastIndexOf('/');
        if (index == -1)
        {
            name = path;
        }
        else if (index == path.length())
        {
            name = "";
        }
        else
        {
            name = path.substring(index + 1);
        }
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
        if (parent == null)
        {
            if (path.contains("/"))
            {
                parent = new RemoteFile(true, null, getParentPath(path));
            }
            else
            {
                if (path.length() > 0)
                {
                    parent = new RemoteFile(true, null, "");
                }
            }
        }

        return parent;
    }

    private String getParentPath(String path)
    {
        int index = path.lastIndexOf('/');
        assert(index >= 0);
        return path.substring(0, index);
    }

    public String getMimeType()
    {
        return type;
    }

    public void setMimeType(String type)
    {
        this.type = type;
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

    public int compareTo(Object o)
    {
        RemoteFile other = (RemoteFile) o;
        return name.compareTo(other.name);
    }

    public String toString()
    {
        return name + (isDir ? "/" : "");
    }
}
