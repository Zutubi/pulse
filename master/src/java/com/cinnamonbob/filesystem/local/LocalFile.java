package com.cinnamonbob.filesystem.local;

import com.cinnamonbob.filesystem.FileNotFoundException;
import com.cinnamonbob.filesystem.File;

/**
 * <class-comment/>
 */
public class LocalFile implements File, Comparable
{
    protected final java.io.File file;
    protected final LocalFileSystem fileSystem;

    protected LocalFile(LocalFileSystem fileSystem, java.io.File file)
    {
        this.file = file;
        this.fileSystem = fileSystem;
    }

    public boolean isDirectory()
    {
        return file.isDirectory();
    }

    public boolean isFile()
    {
        return file.isFile();
    }

    public LocalFile getParentFile()
    {
        return new LocalFile(fileSystem, file.getParentFile());
    }

    public String getMimeType()
    {
        try
        {
            if (file.exists())
            {
                return fileSystem.getMimeType(this);
            }
            return null;
        }
        catch (FileNotFoundException e)
        {
            // programming error. we are the file, we exist.
            return null;
        }
    }

    public long length()
    {
        return file.length();
    }

    public String getName()
    {
        return file.getName();
    }

    public String getPath()
    {
        return file.getPath();
    }

    public int compareTo(Object o)
    {
        return file.compareTo(((LocalFile)o).file);
    }
}
