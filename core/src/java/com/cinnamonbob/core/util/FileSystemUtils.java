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

        String canonicalDir;

        try
        {
            canonicalDir = dir.getCanonicalPath();
        }
        catch (IOException e)
        {
            return false;
        }

        String[] contents = dir.list();
        assert(contents != null);

        for (String child : contents)
        {
            File file = new File(dir, child);
            String canonicalFile;

            // The canonical path lets us distinguish symlinks from actual
            // directories.
            try
            {
                canonicalFile = file.getCanonicalPath();
            }
            catch (IOException e)
            {
                return false;
            }

            // We don't want to traverse symbolic links to directories.
            // The canonical path tells us where the file really is, and we
            // double check it is under the directory (using the canonical
            // path for the directory too).
            if (file.isDirectory() && canonicalFile.startsWith(canonicalDir))
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

    public static void cleanOutputDir(File output) throws IOException
    {
        if (output.isDirectory())
        {
            if (!FileSystemUtils.removeDirectory(output))
            {
                throw new IOException("Unable to remove existing output directory '" + output.getPath() + "'");
            }
        }

        if (!output.mkdirs())
        {
            throw new IOException("Unable to create output directory '" + output.getPath() + "'");
        }
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

    public static void createDirectory(File file) throws IOException
    {
        if (file.exists())
        {
            if (!file.isDirectory())
            {
                throw new IOException("Can not create directory. File '" + file + "' already exists.");
            }
            return;
        }
        if (!file.mkdirs())
        {
            throw new IOException("Failed to create directory '" + file + "'");
        }
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
            sourcePath = sourcePath.replace('\\', ZIP_SEPARATOR);
            sourcePath = sourcePath.replace(File.separatorChar, ZIP_SEPARATOR);
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
        long modifiedTime = source.lastModified();

        if (source.isDirectory())
        {
            String dirPath = "";
            if (!("".equals(sourcePath)))
            {
                dirPath = sourcePath + ZIP_SEPARATOR;
                ZipEntry entry = new ZipEntry(dirPath);
                entry.setTime(modifiedTime);
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
            entry.setTime(modifiedTime);
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

                Process p = null;

                try
                {
                    // TODO: at the very least, only do this where necessary!
                    ProcessBuilder builder = new ProcessBuilder("chmod", "+x", file.getAbsolutePath());
                    p = builder.start();
                    p.waitFor();
                }
                catch (Exception e)
                {
                    // Ignored
                }
                finally
                {
                    if (p != null)
                    {
                        p.destroy();
                    }
                }
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
            byte [] b = new byte[512];
            int len = 0;
            while ((len = zin.read(b)) != -1)
            {
                out.write(b, 0, len);
            }
        }
        finally
        {
            IOUtils.close(out);
        }
    }

    /**
     *
     * @param src
     * @param dest
     * @param force delete the destination directory if it already exists before renaming.
     *
     * @return true if the rename was successful, false otherwise.
     */
    public static boolean rename(File src, File dest, boolean force)
    {
        if (dest.exists() && force)
        {
            removeDirectory(dest);
        }

        return src.renameTo(dest);
    }

    public static boolean rename(File src, File dest)
    {
        return rename(src, dest, false);
    }

    public static void createFile(File file, String data) throws IOException
    {
        FileOutputStream os = null;
        OutputStreamWriter ow = null;

        try
        {
            os = new FileOutputStream(file);
            ow = new OutputStreamWriter(os);
            ow.write(data);
        }
        finally
        {
            IOUtils.close(ow);
            IOUtils.close(os);
        }
    }
}
