package com.zutubi.pulse.master;

import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.Resource;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.util.ZipUtils;
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.SystemInfo;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.pulse.servercore.services.SlaveService;
import com.zutubi.pulse.servercore.services.SlaveStatus;
import static com.zutubi.pulse.servercore.servlet.DownloadResultsServlet.*;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.Pair;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import org.mortbay.util.UrlEncoded;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 */
public class SlaveAgentService implements AgentService
{
    private static final Logger LOG = Logger.getLogger(SlaveAgentService.class);

    private SlaveService service;
    private AgentConfiguration agentConfig;
    private ResourceManager resourceManager;
    private ServiceTokenManager serviceTokenManager;
    private MasterLocationProvider masterLocationProvider;

    public SlaveAgentService(SlaveService service, AgentConfiguration agentConfig)
    {
        this.service = service;
        this.agentConfig = agentConfig;
    }

    public String getUrl()
    {
        return agentConfig.getHost() + ":" + agentConfig.getPort();
    }

    public int ping()
    {
        return service.ping();
    }

    public SlaveStatus getStatus(String masterLocation)
    {
        return service.getStatus(serviceTokenManager.getToken(), masterLocation);
    }

    public boolean updateVersion(String masterBuild, String masterUrl, long handle, String packageUrl, long packageSize)
    {
        return service.updateVersion(serviceTokenManager.getToken(), masterBuild, masterUrl, handle, packageUrl, packageSize);
    }

    public List<Resource> discoverResources()
    {
        return service.discoverResources(serviceTokenManager.getToken());
    }

    public SystemInfo getSystemInfo()
    {
        return service.getSystemInfo(serviceTokenManager.getToken());
    }

    public List<CustomLogRecord> getRecentMessages()
    {
        return service.getRecentMessages(serviceTokenManager.getToken());
    }

    public boolean hasResource(ResourceRequirement requirement)
    {
        return resourceManager.getAgentRepository(agentConfig).hasResource(requirement);
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

    public void collectResults(long projectHandle, String project, long recipeId, boolean incremental, String persistentPattern, File outputDest, File workDest)
    {
        collect(projectHandle, project, recipeId, incremental, persistentPattern, true, outputDest);
        if (workDest != null)
        {
            collect(projectHandle, project, recipeId, incremental, persistentPattern, false, workDest);
        }
    }

    private void collect(long projectHandle, String project, long recipeId, boolean incremental, String persistentPattern, boolean output, File destination)
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

            String query = buildQueryString(asPair(PARAM_TOKEN, serviceTokenManager.getToken()),
                                            asPair(PARAM_PROJECT_HANDLE, Long.toString(projectHandle)),
                                            asPair(PARAM_PROJECT, project),
                                            asPair(PARAM_RECIPE_ID, Long.toString(recipeId)),
                                            asPair(PARAM_INCREMENTAL, Boolean.toString(incremental)),
                                            asPair(PARAM_PERSISTENT_PATTERN, persistentPattern),
                                            asPair(PARAM_OUTPUT, Boolean.toString(output)));

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
            ZipUtils.extractZip(zipFile, tempDir);

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

    private String buildQueryString(Pair<String, String>... params)
    {
        StringBuilder sb = new StringBuilder();
        for (Pair<String, String> param: params)
        {
            if (sb.length() > 0)
            {
                sb.append('&');
            }

            sb.append(param.first);
            sb.append('=');
            sb.append(UrlEncoded.encodeString(param.second));
        }

        return sb.toString();
    }

    public void cleanup(long projectHandle, String project, long recipeId, boolean incremental, String persistentPattern)
    {
        try
        {
            service.cleanupRecipe(serviceTokenManager.getToken(), projectHandle, project, recipeId, incremental, persistentPattern);
        }
        catch (Exception e)
        {
            LOG.warning("Failed to cleanup recipe '" + recipeId + "' on slave '" + agentConfig.getName() + "'", e);
        }
    }

    public void terminateRecipe(long recipeId)
    {
        try
        {
            service.terminateRecipe(serviceTokenManager.getToken(), recipeId);
        }
        catch (RuntimeException e)
        {
            LOG.severe("Unable to terminate recipe: " + e.getMessage(), e);
        }
    }

    public String getHostName()
    {
        return agentConfig.getName();
    }

    public void garbageCollect()
    {
        service.garbageCollect();
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

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
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
