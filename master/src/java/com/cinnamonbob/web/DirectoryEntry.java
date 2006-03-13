package com.cinnamonbob.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;

/**
 */
public class DirectoryEntry
{
    private String path;
    private boolean isDirectory;
    private String name;
    private String mimeType;
    private long size;

    public DirectoryEntry(File file, String name, String path)
    {
        this.path = path;
        isDirectory = file.isDirectory();
        if(isDirectory)
        {
            mimeType = "directory";
        }
        else
        {
            mimeType = guessMimeType(name, file);
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

        if(size > 1024 * 1024)
        {
            s = size / (1024 * 1024.0);
            units = "MB";
        }
        else if(size > 1024)
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

    public static String guessMimeType(String name, File file)
    {
        String type = URLConnection.guessContentTypeFromName(name);
        if(type == null)
        {
            try
            {
                type = URLConnection.guessContentTypeFromStream(new FileInputStream(file));
            }
            catch (IOException e)
            {
                // Oh well
            }

            if(type == null)
            {
                type = "text/plain";
            }
        }

        return type;
    }

}
