package com.zutubi.pulse.servercore.filesystem;

/**
 * The FileInfo is a value object used to hold information file
 * related information for serialisation via hessian.
 */
public class FileInfo
{
    private boolean exists;
    private boolean hidden;
    private boolean directory;
    private boolean file;
    private String name;
    private long length;

    public FileInfo()
    {
        // for hessian.
    }

    public FileInfo(java.io.File f)
    {
        exists = f.exists();
        hidden = f.isHidden();
        file = f.isFile();
        directory = f.isDirectory();
        name = f.getName();
        length = f.length();
    }

    public boolean exists()
    {
        return exists;
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

    public String getName()
    {
        return name;
    }

    public long length()
    {
        return length;
    }
}
