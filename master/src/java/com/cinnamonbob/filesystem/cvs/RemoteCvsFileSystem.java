package com.cinnamonbob.filesystem.cvs;

import com.cinnamonbob.filesystem.File;
import com.cinnamonbob.filesystem.FileNotFoundException;
import com.cinnamonbob.filesystem.FileSystem;
import com.cinnamonbob.filesystem.FileSystemException;
import com.cinnamonbob.filesystem.local.LocalFile;
import com.cinnamonbob.model.Cvs;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.cvs.CvsServer;

import java.io.InputStream;

/**
 * <class-comment/>
 */
public class RemoteCvsFileSystem implements FileSystem
{
    private final Cvs cvs;
    private final CvsServer server;

    public RemoteCvsFileSystem(Cvs cvs) throws SCMException
    {
        this.cvs = cvs;
        this.server = (CvsServer) cvs.createServer();
    }

    public InputStream getFileContents(String path) throws FileSystemException
    {
        return null;
    }

    public InputStream getFileContents(File file) throws FileSystemException
    {
        return null;
    }

    public File getFile(String path) throws FileSystemException
    {
        return null;
    }

    public String getMimeType(String path) throws FileSystemException
    {
        return null;
    }

    public String getMimeType(File file) throws FileNotFoundException
    {
        return null;
    }

    public File[] list(String path)
    {
        return new File[0];
    }

    public File[] list(File dir)
    {
        return new File[0];
    }
}
