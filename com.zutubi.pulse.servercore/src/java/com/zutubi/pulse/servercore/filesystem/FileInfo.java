package com.zutubi.pulse.servercore.filesystem;

/**
 * <class comment/>
 */
public class FileInfo
{
    private boolean hidden;
    private boolean directory;
    private boolean file;
    private String name;
    private String absolutePath;
    private String separator;
    private String path;
    private long length;

    private String[] list;

    public FileInfo()
    {
        // for hessian.
    }

    public FileInfo(java.io.File f)
    {
        hidden = f.isHidden();
        file = f.isFile();
        directory = f.isDirectory();
        name = f.getName();
        absolutePath = f.getAbsolutePath();
        separator = java.io.File.separator;
        path = f.getPath();
        length = f.length();
        list = f.list();
    }

    public String getSeparator()
    {
        return separator;
    }

    public boolean isHidden()
    {
        return hidden;
    }

    public boolean isDirectory()
    {
        return directory;
    }

    public boolean isFile()
    {
        return file;
    }

    public String getPath()
    {
        return path;
    }

    public String getName()
    {
        return name;
    }

    public String getAbsolutePath()
    {
        return absolutePath;
    }

    public long length()
    {
        return length;
    }

    public String[] list()
    {
        return list;
    }
}
