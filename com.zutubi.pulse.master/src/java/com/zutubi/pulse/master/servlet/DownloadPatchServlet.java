package com.zutubi.pulse.master.servlet;

import com.google.common.io.ByteStreams;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.servercore.services.InvalidTokenException;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 */
public class DownloadPatchServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(DownloadPatchServlet.class);
    private MasterConfigurationManager configurationManager;
    private ServiceTokenManager serviceTokenManager;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            String token = request.getParameter("token");
            try
            {
                serviceTokenManager.validateToken(token);
            }
            catch (InvalidTokenException e)
            {
                response.sendError(403, "Invalid token");
                return;
            }

            long userId = Long.parseLong(request.getParameter("user"));
            long number = Long.parseLong(request.getParameter("number"));

            MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
            File patchFile = paths.getUserPatchFile(userId, number);

            try
            {
                response.setContentType("application/x-octet-stream");
                response.setContentLength((int) patchFile.length());

                FileInputStream input = null;

                try
                {
                    input = new FileInputStream(patchFile);
                    ByteStreams.copy(input, response.getOutputStream());
                }
                finally
                {
                    IOUtils.close(input);
                }

                response.getOutputStream().flush();
            }
            catch (FileNotFoundException e)
            {
                DownloadPatchServlet.LOG.warning(e);
                response.sendError(404, "File not found: " + e.getMessage());
            }
            catch (IOException e)
            {
                DownloadPatchServlet.LOG.warning(e);
                response.sendError(500, "I/O error: " + e.getMessage());
            }
        }
        catch (NumberFormatException e)
        {
            try
            {
                response.sendError(500, "Invalid parameter");
            }
            catch (IOException e1)
            {
                DownloadPatchServlet.LOG.warning(e1);
            }
        }
        catch (IOException e)
        {
            DownloadPatchServlet.LOG.warning(e);
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }
}
