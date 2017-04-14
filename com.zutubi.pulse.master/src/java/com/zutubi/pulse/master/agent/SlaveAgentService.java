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

package com.zutubi.pulse.master.agent;

import com.google.common.io.ByteStreams;
import com.zutubi.pulse.core.PulseExecutionContext;
import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.AgentRecipeDetails;
import com.zutubi.pulse.servercore.agent.SynchronisationMessage;
import com.zutubi.pulse.servercore.agent.SynchronisationMessageResult;
import com.zutubi.pulse.servercore.filesystem.FileInfo;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.pulse.servercore.services.SlaveService;
import com.zutubi.util.SecurityUtils;
import com.zutubi.util.WebUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.zutubi.pulse.servercore.servlet.DownloadResultsServlet.*;

/**
 * Service for communicating with agents run on slaves.  Wraps the more general
 * slave service, taking care of providing the token and agent name where
 * required.
 */
public class SlaveAgentService implements AgentService
{
    private static final Logger LOG = Logger.getLogger(SlaveAgentService.class);

    private SlaveService service;
    private AgentConfiguration agentConfig;
    private ServiceTokenManager serviceTokenManager;
    private MasterLocationProvider masterLocationProvider;
    private SlaveCommandListener slaveCommandListener;

    public SlaveAgentService(SlaveService service, AgentConfiguration agentConfig)
    {
        this.service = service;
        this.agentConfig = agentConfig;
    }

    public boolean build(RecipeRequest request)
    {
        try
        {
            return service.build(serviceTokenManager.getToken(), masterLocationProvider.getMasterUrl(), agentConfig.getHandle(), request);
        }
        catch (RuntimeException e)
        {
            throw convertException("Unable to dispatch recipe request '" + request.getId() + "' to slave '" + agentConfig.getName() + "'", e);
        }
    }

    public void collectResults(AgentRecipeDetails recipeDetails, File destination)
    {
        FileOutputStream fos = null;
        File tempDir = null;

        try
        {
            // We don't want the system to see partially-unzipped directories,
            // so we unzip to a temporary location and rename as the final
            // step.
            tempDir = new File(destination.getAbsolutePath() + ".tmp");
            if(!tempDir.mkdirs())
            {
                tempDir = null;
                throw new BuildException("Unable to create temporary directory '" + tempDir.getAbsolutePath() + "'");
            }

            String query = WebUtils.buildQueryString(PARAM_TOKEN, serviceTokenManager.getToken(),
                                            PARAM_AGENT_HANDLE, Long.toString(recipeDetails.getAgentHandle()),
                                            PARAM_AGENT, recipeDetails.getAgent(),
                                            PARAM_AGENT_DATA_PATTERN, recipeDetails.getAgentDataPattern(),
                                            PARAM_PROJECT_HANDLE, Long.toString(recipeDetails.getProjectHandle()),
                                            PARAM_PROJECT, recipeDetails.getProject(),
                                            PARAM_STAGE_HANDLE, Long.toString(recipeDetails.getStageHandle()),
                                            PARAM_STAGE, recipeDetails.getStage(),
                                            PARAM_RECIPE_ID, Long.toString(recipeDetails.getRecipeId()),
                                            PARAM_INCREMENTAL, Boolean.toString(recipeDetails.isIncremental()),
                                            PARAM_UPDATE, Boolean.toString(recipeDetails.isUpdate()),
                                            PARAM_PERSISTENT_PATTERN, recipeDetails.getProjectPersistentPattern(),
                                            PARAM_TEMP_PATTERN, recipeDetails.getProjectTempPattern(),
                                            PARAM_OUTPUT, Boolean.toString(true));

            URL resultUrl = new URL(agentConfig.isSsl() ? "https" : "http", agentConfig.getHost(), agentConfig.getPort(), "/download?" + query);
            URLConnection urlConnection = resultUrl.openConnection();
            urlConnection.setReadTimeout(300000);
            
            // originally the zip stream was unzipped as read from the
            // servlet, however this resulted in socket errors on the
            // servlet side (I think when the zip was bigger than a
            // buffer).

            // take url connection input stream and write contents to zip file
            File zipFile = new File(destination.getAbsolutePath() + ".zip");
            fos = new FileOutputStream(zipFile);
            ByteStreams.copy(urlConnection.getInputStream(), fos);
            IOUtils.close(urlConnection.getInputStream());
            IOUtils.close(fos);
            fos = null;

            if (RecipeProcessor.DEBUG_RESULT_COLLECTION)
            {
                logZipInfo(zipFile);
            }

            // now unzip the file
            PulseZipUtils.extractZip(zipFile, tempDir);

            if (!RecipeProcessor.DEBUG_RESULT_COLLECTION)
            {
                zipFile.delete();
            }
            
            try
            {
                FileSystemUtils.rename(tempDir, destination, true);
            }
            catch (IOException e)
            {
                throw new BuildException("Renaming result directory: " + e.getMessage(), e);
            }
        }
        catch (IOException e)
        {
            String message = e.getMessage();
            message = message.replaceAll("token=[0-9a-zA-Z]+&", "token=[scrubbed]&");
            throw new BuildException("Error downloading results from agent '" + agentConfig.getName() + ": " + message, e);
        }
        finally
        {
            IOUtils.close(fos);

            if (tempDir != null)
            {
                try
                {
                    FileSystemUtils.rmdir(tempDir);
                }
                catch (IOException e)
                {
                    // Ignore.
                }
            }
        }
    }

    private void logZipInfo(File zipFile)
    {
        try
        {
            LOG.warning("Collected zip '" + zipFile.getAbsolutePath() + "' (Length: " + zipFile.length() + ", MD5: " + SecurityUtils.digest("MD5", zipFile) + ")");
            LOG.warning("Zip retained for debugging");
        }
        catch (Exception e)
        {
            LOG.severe(e);
        }
    }

    public void cleanup(AgentRecipeDetails recipeDetails)
    {
        try
        {
            service.cleanupRecipe(serviceTokenManager.getToken(), recipeDetails);
        }
        catch (Exception e)
        {
            LOG.warning("Failed to cleanup recipe '" + recipeDetails.getRecipeId() + "' on slave '" + agentConfig.getName() + "'", e);
        }
    }

    public void terminateRecipe(long recipeId)
    {
        try
        {
            service.terminateRecipe(serviceTokenManager.getToken(), agentConfig.getHandle(), recipeId);
        }
        catch (RuntimeException e)
        {
            LOG.severe("Unable to terminate recipe: " + e.getMessage(), e);
        }
    }

    public List<SynchronisationMessageResult> synchronise(List<SynchronisationMessage> messages)
    {
        return service.synchronise(serviceTokenManager.getToken(), masterLocationProvider.getMasterUrl(), agentConfig.getAgentStateId(), messages);
    }

    public List<FileInfo> getFileListing(AgentRecipeDetails recipeDetails, String path)
    {
        return service.getFileInfos(serviceTokenManager.getToken(), recipeDetails, path);
    }

    public FileInfo getFile(AgentRecipeDetails recipeDetails, String path)
    {
        return service.getFileInfo(serviceTokenManager.getToken(), recipeDetails, path);    
    }

    public void executeCommand(PulseExecutionContext context, List<String> commandLine, String workingDir, int timeout)
    {
        OutputStream outputStream = context.getOutputStream();
        if (outputStream == null)
        {
            outputStream = ByteStreams.nullOutputStream();
        }

        SlaveCommand command = slaveCommandListener.registerCommandWithStream(outputStream);

        // Now we can clear out the stream info rather than send the stream over hessian.
        context = new PulseExecutionContext(context);
        context.setOutputStream(null);

        try
        {
            service.runCommand(serviceTokenManager.getToken(), masterLocationProvider.getMasterUrl(), context, commandLine, workingDir, command.getId(), timeout);

            // We periodically wake up to check that the agent still knows about the command to guard against cases
            // like an agent reboot during a hook command (and as a defense against bugs communicating the result back
            // to the master).
            while (!command.waitFor(60, TimeUnit.SECONDS))
            {
                if (!service.checkCommand(serviceTokenManager.getToken(), command.getId()))
                {
                    // The slave command events are processed synchronously (both forwarding slave side, and handling
                    // master side), but there is still a window between waitFor returning and checkCommand running
                    // where the command may have completed.  So we run a final waitFor here to ensure we didn't just
                    // get unlucky.
                    if (command.waitFor(1, TimeUnit.MILLISECONDS))
                    {
                        // Hit the race condition, the command is actually done.
                        break;
                    }

                    throw new BuildException("Agent no longer knows about command we are waiting for (possible agent reboot)");
                }
            }
        }
        catch (InterruptedException e)
        {
            throw new BuildException("Interrupted waiting for command to run: " + e.getMessage(), e);
        }
        finally
        {
            slaveCommandListener.unregisterCommand(command.getId());
        }
    }

    public AgentConfiguration getAgentConfig()
    {
        return agentConfig;
    }

    private BuildException convertException(String context, RuntimeException e)
    {
        return new BuildException(context + ": " + e.getMessage(), e);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof SlaveAgentService)
        {
            SlaveAgentService other = (SlaveAgentService) obj;
            return other.getAgentConfig().equals(agentConfig);
        }

        return false;
    }

    public void setServiceTokenManager(ServiceTokenManager serviceTokenManager)
    {
        this.serviceTokenManager = serviceTokenManager;
    }

    public void setMasterLocationProvider(MasterLocationProvider masterLocationProvider)
    {
        this.masterLocationProvider = masterLocationProvider;
    }

    public void setSlaveCommandListener(SlaveCommandListener slaveCommandListener)
    {
        this.slaveCommandListener = slaveCommandListener;
    }
}
