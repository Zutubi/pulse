/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.servercore.servlet;

import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.ServerRecipePaths;
import com.zutubi.pulse.servercore.bootstrap.ConfigurationManager;
import com.zutubi.pulse.servercore.services.InvalidTokenException;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
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
    public static final String PARAM_STAGE_HANDLE = "stageHandle";
    public static final String PARAM_STAGE = "stage";
    public static final String PARAM_RECIPE_ID = "recipeId";
    public static final String PARAM_INCREMENTAL = "incremental";
    public static final String PARAM_UPDATE = "update";
    public static final String PARAM_PERSISTENT_PATTERN = "persistentPattern";
    public static final String PARAM_TEMP_PATTERN = "tempPattern";
    public static final String PARAM_OUTPUT = "output";

    private ConfigurationManager configurationManager;
    private ServiceTokenManager serviceTokenManager;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        AgentRecipeDetails details = new AgentRecipeDetails();
        details.setAgent(request.getParameter(PARAM_AGENT));
        details.setProject(request.getParameter(PARAM_PROJECT));
        details.setStage(request.getParameter(PARAM_STAGE)) ;
        details.setIncremental(Boolean.parseBoolean(request.getParameter(PARAM_INCREMENTAL)));
        details.setUpdate(Boolean.parseBoolean(request.getParameter(PARAM_UPDATE)));
        details.setProjectPersistentPattern(request.getParameter(PARAM_PERSISTENT_PATTERN));
        details.setProjectTempPattern(request.getParameter(PARAM_TEMP_PATTERN));
        details.setAgentDataPattern(request.getParameter(PARAM_AGENT_DATA_PATTERN));

        String agentHandleString = request.getParameter(PARAM_AGENT_HANDLE);
        String projectHandleString = request.getParameter(PARAM_PROJECT_HANDLE);
        String stageHandleString = request.getParameter(PARAM_STAGE_HANDLE);
        String recipeIdString = request.getParameter(PARAM_RECIPE_ID);

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

            details.setAgentHandle(Long.parseLong(agentHandleString));
            details.setProjectHandle(Long.parseLong(projectHandleString));
            details.setStageHandle(Long.parseLong(stageHandleString));
            details.setRecipeId(Long.parseLong(recipeIdString));

            // lookup the recipe location, zip it up and write to output.
            ServerRecipePaths paths = new ServerRecipePaths(details, configurationManager.getUserPaths().getData());
            File zipFile;
            boolean output = Boolean.parseBoolean(request.getParameter(PARAM_OUTPUT));
            if (output)
            {
                zipFile = new File(paths.getOutputDir().getAbsolutePath() + ".zip");
            }
            else
            {
                zipFile = new File(paths.getBaseDir().getAbsolutePath() + ".zip");
            }

            ServletUtils.sendFile(zipFile, response);
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
