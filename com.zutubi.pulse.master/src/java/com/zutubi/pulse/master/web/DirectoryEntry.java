package com.zutubi.pulse.master.web;

import com.zutubi.pulse.servercore.filesystem.File;

/**
 */
public class DirectoryEntry
{
    private String path;
    private boolean isDirectory;
    private String name;
    private String mimeType;
    private long size;

    public DirectoryEntry(File file, String name)
    {
        this.path = file.getPath();
        isDirectory = file.isDirectory();
        if (isDirectory)
        {
            mimeType = "directory";
        }
        else
        {
            mimeType = file.getMimeType();
        }
        this.name = name;
        size = file.length();
    }

    public String getPath()
    {
        return path;
    }

    public boolean isDirectory()
    {
        return isDirectory;
    }

    public String getName()
    {
        return name;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public long getSize()
    {
        return size;
    }

    public String getPrettySize()
    {
        double s;
        String units;

        if (size > 1024 * 1024)
        {
            s = size / (1024 * 1024.0);
            units = "MB";
        }
        else if (size > 1024)
        {
            s = size / 1024.0;
            units = "kB";
        }
        else
        {
            return size + " bytes";
        }

        return String.format("%.02f %s", s, units);
    }
}
