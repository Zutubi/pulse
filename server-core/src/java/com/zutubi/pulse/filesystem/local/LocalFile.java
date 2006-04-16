/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.filesystem.local;

import com.zutubi.pulse.filesystem.File;
import com.zutubi.pulse.filesystem.FileNotFoundException;

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
        String path = file.getPath();
        String basePath = fileSystem.getBase().getAbsolutePath();
        if (path.startsWith(basePath))
        {
            path = path.substring(basePath.length());
        }

        if(path.startsWith(fileSystem.getSeparator()))
        {
            path = path.substring(fileSystem.getSeparator().length());
        }

        return path;
    }

    public int compareTo(Object o)
    {
        return file.compareTo(((LocalFile) o).file);
    }
}
