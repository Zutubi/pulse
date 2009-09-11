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

    public static final String PARAM_TOKEN = "token";
    public static final String PARAM_AGENT_HANDLE = "agentHandle";
    public static final String PARAM_AGENT = "agent";
    public static final String PARAM_AGENT_DATA_PATTERN = "agentDataPattern";
    public static final String PARAM_PROJECT_HANDLE = "projectHandle";
    public static final String PARAM_PROJECT = "project";
    public static final String PARAM_RECIPE_ID = "recipeId";
    public static final String PARAM_INCREMENTAL = "incremental";
    public static final String PARAM_PERSISTENT_PATTERN = "persistentPattern";
    public static final String PARAM_OUTPUT = "output";

    private ConfigurationManager configurationManager;
    private ServiceTokenManager serviceTokenManager;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        String agentHandleString = request.getParameter(PARAM_AGENT_HANDLE);
        String agent = request.getParameter(PARAM_AGENT);
        String agentDataPattern = request.getParameter(PARAM_AGENT_DATA_PATTERN);
        String projectHandleString = request.getParameter(PARAM_PROJECT_HANDLE);
        String project = request.getParameter(PARAM_PROJECT);
        String recipeIdString = request.getParameter(PARAM_RECIPE_ID);
        boolean incremental = Boolean.parseBoolean(request.getParameter(PARAM_INCREMENTAL));
        String persistentPattern = request.getParameter(PARAM_PERSISTENT_PATTERN);

        try
        {
            String token = request.getParameter(PARAM_TOKEN);
            try
            {
                serviceTokenManager.validateToken(token);
            }
            catch (InvalidTokenException e)
            {
                response.sendError(403, "Invalid token");
            }

            long agentHandle = Long.parseLong(agentHandleString);
            long projectHandle = Long.parseLong(projectHandleString);
            long recipeId = Long.parseLong(recipeIdString);
            boolean output = Boolean.parseBoolean(request.getParameter(PARAM_OUTPUT));

            // lookup the recipe location, zip it up and write to output.
            ServerRecipePaths paths = new ServerRecipePaths(agentHandle, agent, agentDataPattern, projectHandle, project, recipeId, incremental, persistentPattern, configurationManager.getUserPaths().getData());
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
                response.sendError(500, "Invalid recipe '" + recipeIdString + "'");
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
