package com.zutubi.pulse.master.servlet;

import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

/**
 * <class comment/>
 */
public class FileServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(FileServlet.class);

    private FileSystemManager fsManager;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            String path = request.getPathInfo();

            path = path.replace("\\", "/");

            if (path.startsWith("/"))
            {
                path = path.substring(1);
            }

            path = "pulse:///" + path;

            AbstractPulseFileObject pfo = (AbstractPulseFileObject) fsManager.resolveFile(path);

            // if the pfo is a file, download it. If it is a folder, list the directory.
            if (pfo.getType() == FileType.FILE)
            {
                doDownload(request, response, pfo);
            }
            else
            {
                doList(request, response);
            }
        }
        catch (IOException e)
        {
            LOG.error(e);
            throw e;
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    private void doList(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.sendError(404, "Can not display requested resource '" + request.getPathInfo() + "', this resource it is not a file.");
    }

    private void doDownload(HttpServletRequest request, HttpServletResponse response, AbstractPulseFileObject pfo) throws IOException
    {
        String filename = pfo.getName().getBaseName();

        String contentType = pfo.getContent().getContentInfo().getContentType();
        if (StringUtils.stringSet(contentType))
        {
            response.setContentType(contentType);
        }
        else
        {
            response.setContentType(URLConnection.guessContentTypeFromName(filename));
        }
        
        response.setContentLength((int) pfo.getContent().getSize());

        InputStream is = null;
        try
        {
            is = pfo.getContent().getInputStream();
            IOUtils.joinStreams(is, response.getOutputStream());
        }
        finally
        {
            // ensure that we close the open file.
            IOUtils.close(is);
        }
    }

    public void setFileSystemManager(FileSystemManager fsManager)
    {
        this.fsManager = fsManager;
    }
}
