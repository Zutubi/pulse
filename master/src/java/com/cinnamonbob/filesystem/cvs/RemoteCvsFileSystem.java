package com.cinnamonbob.filesystem.cvs;

import com.cinnamonbob.core.model.CvsRevision;
import com.cinnamonbob.filesystem.File;
import com.cinnamonbob.filesystem.FileNotFoundException;
import com.cinnamonbob.filesystem.FileSystem;
import com.cinnamonbob.filesystem.FileSystemException;
import com.cinnamonbob.model.Cvs;
import com.cinnamonbob.scm.SCMException;
import com.cinnamonbob.scm.cvs.CvsServer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class RemoteCvsFileSystem implements FileSystem
{
    private final CvsServer server;

    private Map<String, RemoteCvsFile> files = new TreeMap<String, RemoteCvsFile>();

    public RemoteCvsFileSystem(Cvs cvs) throws FileSystemException
    {
        try
        {
            this.server = (CvsServer) cvs.createServer();
            String module = cvs.getModule();
            String branch = null;

            RemoteCvsFile root = new RemoteCvsFile("", true, null, "");
            files.put("", root);
            // break this up into files and directories.
            for (String filename : server.getListing(module, branch))
            {
                StringTokenizer tokens = new StringTokenizer(filename, getSeparator(), false);
                String path = "";
                RemoteCvsFile parent = root;
                while (tokens.hasMoreTokens())
                {
                    String name = tokens.nextToken();
                    path = path + getSeparator() + name;
                    if (!files.containsKey(path))
                    {
                        RemoteCvsFile f = new RemoteCvsFile(name, tokens.hasMoreTokens(), parent, path);
                        if (parent != null)
                        {
                            parent.addChild(f);
                        }
                        files.put(path, f);
                    }
                    parent = files.get(path);
                }
            }
        }
        catch (SCMException e)
        {
            throw new FileSystemException(e);
        }
    }

    public InputStream getFileContents(String path) throws FileSystemException
    {
        return getFileContents(files.get(path));
    }

    public InputStream getFileContents(File file) throws FileSystemException
    {
        try
        {
            return new ByteArrayInputStream(server.checkout(1, CvsRevision.HEAD, file.getPath()).getBytes()) ;
        }
        catch (SCMException e)
        {
            throw new FileSystemException(e);
        }
    }

    public File getFile(String path) throws FileSystemException
    {
        return files.get(path);
    }

    public String getMimeType(String path) throws FileSystemException
    {
        return "text/plain";
    }

    public String getMimeType(File file) throws FileNotFoundException
    {
        return "text/plain";
    }

    public File[] list(String path)
    {
        return list(files.get(path));
    }

    public File[] list(File dir)
    {
        if (!dir.isDirectory())
        {
            return new RemoteCvsFile[0];
        }
        List<RemoteCvsFile> remoteCvsFiles = ((RemoteCvsFile) dir).list();
        return remoteCvsFiles.toArray(new RemoteCvsFile[remoteCvsFiles.size()]);
    }

    public String getSeparator()
    {
        return "/";
    }
}
