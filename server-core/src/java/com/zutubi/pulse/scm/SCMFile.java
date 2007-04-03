package com.zutubi.pulse.scm;

/**
 * Represents a file or directory stored in an SCM.
 */
public class SCMFile implements Comparable
{
    private static final String SEPARATOR = "/";
    
    private String name;
    private String path;
    private boolean isDir;
    private SCMFile parent;
    private String type = "text/plain";

    public SCMFile(String name, boolean isDirectory, String path)
    {
        this.name = name;
        this.isDir = isDirectory;

        if (path.endsWith(SEPARATOR))
        {
            path = path.substring(0, path.length() - 1);
        }
        this.path = path;
    }

    public SCMFile(boolean isDirectory, String path)
    {
        this(null, isDirectory, path);

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

    public SCMFile(String name, boolean isDirectory, SCMFile parent, String path)
    {
        this(name, isDirectory, path);
        this.parent = parent;
    }

    public SCMFile(boolean isDirectory, SCMFile parent, String path)
    {
        this(isDirectory, path);
        this.parent = parent;
    }

    public boolean isDirectory()
    {
        return isDir;
    }

    public boolean isFile()
    {
        return !isDirectory();
    }

    public SCMFile getParentFile()
    {
        if (parent == null)
        {
            if (path.contains(SEPARATOR))
            {
                parent = new SCMFile(true, getParentPath(path));
            }
            else
            {
                if (path.length() > 0)
                {
                    parent = new SCMFile(true, "");
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

    public String getAbsolutePath()
    {
        return null;
    }

    public int compareTo(Object o)
    {
        SCMFile other = (SCMFile) o;
        return name.compareTo(other.name);
    }

    public String toString()
    {
        return name + (isDir ? SEPARATOR : "");
    }
}
