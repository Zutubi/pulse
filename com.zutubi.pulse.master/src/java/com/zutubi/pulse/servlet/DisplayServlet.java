package com.zutubi.pulse.servlet;

import com.zutubi.pulse.core.spring.SpringComponentContext;
import com.zutubi.util.logging.Logger;
import com.zutubi.pulse.vfs.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.vfs.pulse.AddressableFileObject;
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

            AbstractPulseFileObject pfo;
            try
            {
                pfo = (AbstractPulseFileObject) getFS().resolveFile(path);
            }
            catch(FileSystemException e)
            {
                // OK, try replacing pluses with spaces
                path = path.replace('+', ' ');
                pfo = (AbstractPulseFileObject) getFS().resolveFile(path);
            }

            if (!(pfo instanceof AddressableFileObject))
            {
                response.sendError(404, String.format("The path '%s' does not represent an addressable resource.", request.getPathInfo()));
                return;
            }
            
            AddressableFileObject afo = (AddressableFileObject) pfo;
            String url = afo.getUrlPath();
            if (afo.isLocal())
            {
                if (!url.startsWith("/"))
                {
                    url = "/" + url;
                }
                url = request.getContextPath() + url;
            }
            response.sendRedirect(url);
        }
        catch (FileSystemException e)
        {
            LOG.error(e);
            String message = e.getMessage();
            if (message.startsWith("Unknown message with code"))
            {
                message = e.getCode();
            }
            response.sendError(404, message);
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
            fsManager = (FileSystemManager) SpringComponentContext.getBean("fileSystemManager");
        }
        return fsManager;
    }

    public void setFileSystemManager(FileSystemManager fsManager)
    {
        this.fsManager = fsManager;
    }
}
