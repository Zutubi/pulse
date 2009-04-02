package com.zutubi.pulse.master;

import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.core.config.ResourceConfiguration;
import com.zutubi.pulse.core.config.ResourceRequirement;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.pulse.master.agent.MasterLocationProvider;
import com.zutubi.pulse.master.model.ResourceManager;
import com.zutubi.pulse.master.tove.config.agent.AgentConfiguration;
import com.zutubi.pulse.servercore.SystemInfo;
import com.zutubi.pulse.servercore.services.ServiceTokenManager;
import com.zutubi.pulse.servercore.services.SlaveService;
import com.zutubi.pulse.servercore.services.SlaveStatus;
import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.util.FileSystemUtils;
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

    public List<ResourceConfiguration> discoverResources()
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

    public void collectResults(String project, long recipeId, boolean incremental, File outputDest, File workDest)
    {
        collect(project, recipeId, incremental, true, outputDest);
        if (workDest != null)
        {
            collect(project, recipeId, incremental, false, workDest);
        }
    }

    private void collect(String project, long recipeId, boolean incremental, boolean output, File destination)
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

            URL resultUrl = new URL("http", agentConfig.getHost(), agentConfig.getPort(), "/download?token=" + serviceTokenManager.getToken() + "&project=" + UrlEncoded.encodeString(project) + "&incremental=" + incremental + "&output=" + output + "&recipe=" + recipeId);
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

            if(!tempDir.renameTo(destination))
            {
                throw new BuildException("Unable to rename result directory to '" + destination.getAbsolutePath() + "'");
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

            if(tempDir != null)
            {
                FileSystemUtils.rmdir(tempDir);
            }
        }
    }

    public void cleanup(String project, long recipeId, boolean incremental)
    {
        try
        {
            service.cleanupRecipe(serviceTokenManager.getToken(), project, recipeId, incremental);
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
