/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.filesystem.local;

import com.zutubi.pulse.filesystem.FileNotFoundException;
import com.zutubi.pulse.filesystem.FileSystem;
import com.zutubi.pulse.filesystem.FileSystemException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class LocalFileSystem implements FileSystem
{
    private final File base;

    public LocalFileSystem(File base)
    {
        this.base = base;
    }

    /**
     * Retrieve the specified file
     *
     * @param path
     * @return an input stream containing the contents of the file.
     */
    public InputStream getFileContents(String path) throws FileSystemException
    {
        return internalGetFileContents(new File(base, path));
    }

    private InputStream internalGetFileContents(File file) throws FileSystemException
    {
        try
        {
            if (!file.exists())
            {
                throw new FileNotFoundException();
            }
            if (!file.isFile())
            {
                throw new FileSystemException();
            }
            return new FileInputStream(file);
        }
        catch (java.io.FileNotFoundException e)
        {
            throw new FileNotFoundException();
        }
    }

    public InputStream getFileContents(com.zutubi.pulse.filesystem.File file) throws FileSystemException
    {
        return internalGetFileContents(((LocalFile) file).file);
    }

    public com.zutubi.pulse.filesystem.File getFile(String path) throws FileSystemException
    {
        return new LocalFile(this, new File(base, path));
    }

    /**
     * Attempt to determine the mime type of the requested file.
     *
     * @param path
     * @return the files mime type, or null if it could not be determined.
     */
    public String getMimeType(String path) throws FileSystemException
    {
        return internalGetMimeType(new File(base, path));
    }

    private String internalGetMimeType(File file) throws FileNotFoundException
    {
        if (!file.exists())
        {
            throw new FileNotFoundException();
        }

        String type = URLConnection.guessContentTypeFromName(file.getName());
        if (type == null)
        {
            try
            {
                type = URLConnection.guessContentTypeFromStream(new FileInputStream(file));
            }
            catch (IOException e)
            {
                // Oh well
            }

            if (type == null)
            {
                type = "text/plain";
            }
        }

        return type;
    }

    public String getMimeType(com.zutubi.pulse.filesystem.File file) throws FileNotFoundException
    {
        return internalGetMimeType(((LocalFile) file).file);
    }

    /**
     * Retrieve a listing of the specified path.
     *
     * @param path
     * @return a list of file handles located at the specified path.
     */
    public LocalFile[] list(String path) throws FileSystemException
    {
        return internalList(new File(base, path));
    }

    public com.zutubi.pulse.filesystem.File[] list(com.zutubi.pulse.filesystem.File dir) throws FileSystemException
    {
        return internalList(((LocalFile) dir).file);
    }

    public String getSeparator()
    {
        return File.separator;
    }

    private LocalFile[] internalList(File dir) throws FileSystemException
    {
        if (!dir.exists())
        {
            throw new FileNotFoundException();
        }
        if (!dir.isDirectory())
        {
            throw new FileSystemException();
        }

        List<LocalFile> listing = new LinkedList<LocalFile>();

        File[] files = dir.listFiles();
        for (File file : files)
        {
            listing.add(new LocalFile(this, file));
        }
        return listing.toArray(new LocalFile[listing.size()]);
    }

    public File getBase()
    {
        return base;
    }
}
