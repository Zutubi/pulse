package com.zutubi.pulse.master.agent;

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
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.WebUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static com.zutubi.pulse.servercore.servlet.DownloadResultsServlet.*;
import static com.zutubi.util.CollectionUtils.asPair;

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

            String query = WebUtils.buildQueryString(asPair(PARAM_TOKEN, serviceTokenManager.getToken()),
                                            asPair(PARAM_AGENT_HANDLE, Long.toString(recipeDetails.getAgentHandle())),
                                            asPair(PARAM_AGENT, recipeDetails.getAgent()),
                                            asPair(PARAM_AGENT_DATA_PATTERN, recipeDetails.getAgentDataPattern()),
                                            asPair(PARAM_PROJECT_HANDLE, Long.toString(recipeDetails.getProjectHandle())),
                                            asPair(PARAM_PROJECT, recipeDetails.getProject()),
                                            asPair(PARAM_STAGE_HANDLE, Long.toString(recipeDetails.getStageHandle())),
                                            asPair(PARAM_STAGE, recipeDetails.getStage()),
                                            asPair(PARAM_RECIPE_ID, Long.toString(recipeDetails.getRecipeId())),
                                            asPair(PARAM_INCREMENTAL, Boolean.toString(recipeDetails.isIncremental())),
                                            asPair(PARAM_PERSISTENT_PATTERN, recipeDetails.getProjectPersistentPattern()),
                                            asPair(PARAM_OUTPUT, Boolean.toString(true)));

            URL resultUrl = new URL("http", agentConfig.getHost(), agentConfig.getPort(), "/download?" + query);
            URLConnection urlConnection = resultUrl.openConnection();
            urlConnection.setReadTimeout(300000);
            
            // originally the zip stream was unzipped as read from the
            // servlet, however this resulted in socket errors on the
            // servlet side (I think when the zip was bigger than a
            // buffer).

            // take url connection input stream and write contents to zip file
            File zipFile = new File(destination.getAbsolutePath() + ".zip");
            fos = new FileOutputStream(zipFile);
            IOUtils.joinStreams(urlConnection.getInputStream(), fos);
            IOUtils.close(urlConnection.getInputStream());
            IOUtils.close(fos);
            fos = null;

            // now unzip the file
            PulseZipUtils.extractZip(zipFile, tempDir);

            zipFile.delete();

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
                FileSystemUtils.rmdir(tempDir);
            }
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
        return service.synchronise(messages);
    }

    public List<FileInfo> getFileListing(AgentRecipeDetails recipeDetails, String path)
    {
        return service.getFileInfos(serviceTokenManager.getToken(), recipeDetails, path);
    }

    public FileInfo getFile(AgentRecipeDetails recipeDetails, String path)
    {
        return service.getFileInfo(serviceTokenManager.getToken(), recipeDetails, path);    
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
}
