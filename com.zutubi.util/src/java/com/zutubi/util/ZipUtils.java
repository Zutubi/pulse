package com.zutubi.util;

import com.zutubi.util.io.IOUtils;
import static com.zutubi.util.io.IOUtils.joinStreams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Basic utilities for zip files.
 */
public class ZipUtils
{
    /**
     * Extracts the files from teh given zip stream to into the given
     * destination directory.
     *
     * @param zin zip stream to exract files from
     * @param dir destination directory
     * @throws IOException on error
     */
    public static void extractZip(ZipInputStream zin, File dir) throws IOException
    {
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null)
        {
            File file = new File(dir, entry.getName());

            if (entry.isDirectory())
            {
                file.mkdirs();
            }
            else
            {
                // ensure that the files parents already exist.
                if (!file.getParentFile().isDirectory())
                {
                    file.getParentFile().mkdirs();
                }
                unzip(zin, file);
            }

            file.setLastModified(entry.getTime());
        }
    }

    private static void unzip(InputStream zin, File file) throws IOException
    {
        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(file);
            joinStreams(zin, out);
        }
        finally
        {
            IOUtils.close(out);
        }
    }
}
