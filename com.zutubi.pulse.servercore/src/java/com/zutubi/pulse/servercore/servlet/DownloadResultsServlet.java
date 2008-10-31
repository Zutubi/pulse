package com.zutubi.pulse.servercore.servlet;

import com.zutubi.pulse.servercore.ServerRecipePaths;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
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
 * A servlet used to download build results from an agent.  The results can
 * be either the artifacts or the working directory snapshot.
 */
public class DownloadResultsServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(DownloadResultsServlet.class);
    private ConfigurationManager configurationManager;
    private ServiceTokenManager serviceTokenManager;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        String project = request.getParameter("project");
        String id = request.getParameter("recipe");
        boolean incremental = Boolean.parseBoolean(request.getParameter("incremental"));

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
            }

            long recipeId = Long.parseLong(id);
            boolean output = Boolean.parseBoolean(request.getParameter("output"));

            // lookup the recipe location, zip it up and write to output.
            ServerRecipePaths paths = new ServerRecipePaths(project, recipeId, configurationManager.getUserPaths().getData(), incremental);
            File zipFile;

            if (output)
            {
                zipFile = new File(paths.getOutputDir().getAbsolutePath() + ".zip");
            }
            else
            {
                zipFile = new File(paths.getBaseDir().getAbsolutePath() + ".zip");
            }

            try
            {
                response.setContentType("application/x-octet-stream");
                response.setContentLength((int) zipFile.length());

                FileInputStream input = null;

                try
                {
                    input = new FileInputStream(zipFile);
                    IOUtils.joinStreams(input, response.getOutputStream());
                }
                finally
                {
                    IOUtils.close(input);
                }

                response.getOutputStream().flush();
            }
            catch (FileNotFoundException e)
            {
                LOG.warning(e);
                response.sendError(404, "File not found: " + e.getMessage());
            }
            catch (IOException e)
            {
                LOG.warning(e);
            }
        }
        catch (NumberFormatException e)
        {
            try
            {
                response.sendError(500, "Invalid recipe '" + id + "'");
            }
            catch (IOException e1)
            {
                LOG.warning(e1);
            }
        }
        catch (IOException e)
        {
            LOG.warning(e);
        }
    }

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }
}
