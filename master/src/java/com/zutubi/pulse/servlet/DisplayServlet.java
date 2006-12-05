package com.zutubi.pulse.servlet;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.vfs.pulse.*;
import com.zutubi.pulse.util.logging.Logger;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <class comment/>
 */
public class DisplayServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(DisplayServlet.class);

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

            if (!path.startsWith("pulse://"))
            {
                path = "pulse:///" + path;
            }

            AbstractPulseFileObject pfo = (AbstractPulseFileObject) getFS().resolveFile(path);
            if (!(pfo instanceof AddressableFileObject))
            {
                response.sendRedirect(request.getContextPath());
                return;
            }
            
            AddressableFileObject afo = (AddressableFileObject) pfo;
            String url = afo.getUrlPath();
            if (!url.startsWith("/"))
            {
                url = "/" + url;
            }
            url = request.getContextPath() + url;
            response.sendRedirect(url);
        }
        catch (Exception e)
        {
            LOG.error(e);
            throw new ServletException(e);
        }
    }

    protected FileSystemManager getFS() throws FileSystemException
    {
        if (fsManager == null)
        {
            fsManager = (FileSystemManager) ComponentContext.getBean("fileSystemManager");
        }
        return fsManager;
    }

    public void setFileSystemManager(FileSystemManager fsManager)
    {
        this.fsManager = fsManager;
    }
}
