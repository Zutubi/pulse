package com.zutubi.prototype.type.record.store;

import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
public class DefaultFS implements FS
{
    public boolean exists(File file)
    {
        return file.exists();
    }

    public boolean createNewFile(File file) throws IOException
    {
        return file.createNewFile();
    }

    public boolean mkdirs(File file)
    {
        return file.mkdirs();
    }

    public boolean delete(File file)
    {
        return file.delete();
    }

    public boolean renameTo(File source, File destination)
    {
        return source.renameTo(destination);
    }
}
