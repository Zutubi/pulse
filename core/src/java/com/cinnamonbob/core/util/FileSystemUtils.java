package com.cinnamonbob.core.util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Miscellaneous utilities for manipulating the file system.
 *
 * @author jsankey
 */
public class FileSystemUtils
{
    private static final char ZIP_SEPARATOR = '/';

    /**
     * Recursively delete a directory and its contents.
     *
     * @param dir the directory to delete
     * @return true iff the whole directory was successfully delete
     */
    public static boolean removeDirectory(File dir)
    {
        if (!dir.exists())
        {
            return true;
        }
        if (!dir.isDirectory())
        {
            return false;
        }

        String[] contents = dir.list();

        assert(contents != null);

        for (String child : contents)
        {
            File file = new File(dir, child);
            String canonical;

            // The canonical path lets us distinguish symlinks from actual
            // directories.
            try
            {
                canonical = file.getCanonicalPath();
            }
            catch (IOException e)
            {
                return false;
            }

            // TODO: Fix the following condition - fails in windows since because of 8.3 file names
            // do not follow symlinks when deleting directories.            
            if (file.isDirectory()) // && canonical.equals(file.getAbsolutePath()))
            {
                if (!removeDirectory(file))
                {
                    return false;
                }
            }
            else
            {
                if (!file.delete())
                {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    public static File createTempDirectory(String prefix, String suffix) throws IOException
    {
        File file = File.createTempFile(prefix, suffix);
        if (!file.exists())
        {
            throw new IOException();
        }
        if (!file.delete())
        {
            throw new IOException();
        }
        if (!file.mkdirs())
        {
            throw new IOException();
        }
        return file;
    }

    public static boolean isParentOf(File parent, File child) throws IOException
    {
        String parentPath = parent.getCanonicalPath();
        String childPath = child.getCanonicalPath();

        return childPath.startsWith(parentPath);
    }

    public static void createZip(File zipFile, File base, File source) throws IOException
    {
        if (!source.exists())
        {
            throw new FileNotFoundException("Source file '" + source.getAbsolutePath() + "' does not exist");
        }

        if (!isParentOf(base, source))
        {
            throw new IOException("Base '" + base.getAbsolutePath() + "' is not a parent of source '" + source.getAbsolutePath() + "'");
        }

        ZipOutputStream os = null;

        try
        {
            os = new ZipOutputStream(new FileOutputStream(zipFile));
            String sourcePath = source.getAbsolutePath().substring(base.getAbsolutePath().length());
            // The additional check for slashes is for systems that may accept something other than
            // their canonical file separator (e.g. '/' is acceptable on Windows despite '\' being
            // the canonical separator).
            sourcePath = sourcePath.replace('\\', '/');
            sourcePath = sourcePath.replace(File.separatorChar, '/');
            if (sourcePath.startsWith("/"))
            {
                sourcePath = sourcePath.substring(1);
            }
            addToZip(os, base, sourcePath);
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    private static void addToZip(ZipOutputStream os, File base, String sourcePath) throws IOException
    {
        File source = new File(base, sourcePath);

        if (source.isDirectory())
        {
            String dirPath = "";
            if (!("".equals(sourcePath)))
            {
                dirPath = sourcePath + ZIP_SEPARATOR;
                ZipEntry entry = new ZipEntry(dirPath);
                os.putNextEntry(entry);
            }

            String[] files = source.list();

            for (String filename : files)
            {
                String path = dirPath + filename;
                addToZip(os, base, path);
            }
        }
        else
        {
            ZipEntry entry = new ZipEntry(sourcePath);
            os.putNextEntry(entry);

            FileInputStream is = null;

            try
            {
                is = new FileInputStream(source);
                IOUtils.joinStreams(is, os);
            }
            finally
            {
                IOUtils.close(is);
            }

        }
    }

    public static void extractZip(ZipInputStream zin, File dir) throws IOException
    {
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null)
        {
            File file = new File(dir, entry.getName());
            if (entry.isDirectory())
            {
                file.mkdir();
            }
            else
            {
                unzip(zin, file);
            }
        }
        zin.close();
    }

    public static void unzip(InputStream zin, File file) throws IOException
    {
        FileOutputStream out = new FileOutputStream(file);
        byte [] b = new byte[512];
        int len = 0;
        while ((len = zin.read(b)) != -1)
        {
            out.write(b, 0, len);
        }
        out.close();
    }
}
