package com.zutubi.pulse.util;

import java.io.File;
import java.io.IOException;

/**
 * <class comment/>
 */
public class CopyUtils
{
    public static void copy(File dest, File... src) throws IOException
    {
        if (dest.isFile())
        {
            // fail.
            throw new IllegalArgumentException(String.format("Copy failed. Can not copy to '%s', it is a file.", dest.getAbsolutePath()));
        }

        if (src.length == 0)
        {
            return;
        }

        // copy a source file to a destination file.
        if (src.length == 1 && !dest.exists())
        {
            // copy as a file.
            if (!dest.getParentFile().exists() && !dest.getParentFile().mkdirs())
            {
                throw new IOException(String.format("Copy failed. Failed to create dir %s", dest.getParentFile().getAbsolutePath()));
            }
            internalCopy(src[0], dest);
            return;
        }

        if (!dest.exists() && !dest.mkdirs())
        {
            throw new IOException(String.format("Copy failed. Failed to create dir %s", dest.getAbsolutePath()));
        }

        for (File f : src)
        {
            // copy into dest directory.
            internalCopy(f, new File(dest, f.getName()));
        }
    }

    protected static void internalCopy(File src, File dest) throws IOException
    {
        if (src.isDirectory())
        {
            if (!dest.isDirectory() && !dest.mkdirs())
            {
                throw new IOException(String.format("Copy failed. Failed to create dir %s", dest.getAbsolutePath()));
            }

            for (String file : src.list())
            {
                internalCopy(new File(src, file), new File(dest, file));
            }
        }
        else
        {
            if (dest.isFile())
            {
                // trouble..
                throw new IOException(String.format("Copy failed. Failed to copy to file %s, it already exists.", dest.getAbsolutePath()));
            }
            if (!dest.createNewFile())
            {
                throw new IOException(String.format("Copy failed. Failed to create file %s", dest.getAbsolutePath()));
            }
            IOUtils.copyFile(src, dest);
        }
    }
}
