package com.cinnamonbob.filesystem.remote;

import com.cinnamonbob.filesystem.File;
import com.cinnamonbob.filesystem.FileNotFoundException;
import com.cinnamonbob.filesystem.FileSystem;
import com.cinnamonbob.filesystem.FileSystemException;
import com.cinnamonbob.model.Scm;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.SCMServer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * <class-comment/>
 */
public class RemoteScmFileSystem implements FileSystem
{
    private final SCMServer server;


    public RemoteScmFileSystem(Scm scm) throws FileSystemException
    {
        try
        {
            this.server = scm.createServer();
        }
        catch (SCMException e)
        {
            throw new FileSystemException("Could not create SCM connection: " + e.getMessage(), e);
        }
    }

    public InputStream getFileContents(String path) throws FileSystemException
    {
        try
        {
            return new ByteArrayInputStream(server.checkout(1, null, path).getBytes());
        }
        catch (SCMException e)
        {
            throw new FileSystemException("Unable to checkout file: " + e.getMessage(), e);
        }
    }

    public InputStream getFileContents(File file) throws FileSystemException
    {
        return getFileContents(file.getPath());
    }

    public File getFile(String path) throws FileSystemException
    {
        try
        {
            return server.getFile(path);
        }
        catch (SCMException e)
        {
            throw new FileSystemException("Unable to retrieve file details: " + e.getMessage(), e);
        }
    }

    public String getMimeType(String path) throws FileSystemException
    {
        return "text/plain";
    }

    public String getMimeType(File file) throws FileNotFoundException
    {
        return "text/plain";
    }

    public File[] list(String path) throws FileSystemException
    {
        try
        {
            List<RemoteFile> files = server.getListing(path);
            return files.toArray(new RemoteFile[files.size()]);
        }
        catch (SCMException e)
        {
            throw new FileSystemException(e.getMessage(), e);
        }
    }

    public File[] list(File dir) throws FileSystemException
    {
        return list(dir.getPath());
    }

    public String getSeparator()
    {
        return "/";
    }
}
