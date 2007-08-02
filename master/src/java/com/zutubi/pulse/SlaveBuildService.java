package com.zutubi.pulse;

import com.zutubi.pulse.agent.MasterAgent;
import com.zutubi.pulse.bootstrap.MasterConfiguration;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SystemConfiguration;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.ZipUtils;
import com.zutubi.pulse.util.logging.Logger;
import org.mortbay.util.UrlEncoded;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 */
public class SlaveBuildService implements BuildService
{
    private static final Logger LOG = Logger.getLogger(SlaveBuildService.class);

    private SlaveService service;
    private Slave slave;
    private MasterConfigurationManager configurationManager;
    private ResourceManager resourceManager;
    private ServiceTokenManager serviceTokenManager;

    public SlaveBuildService(SlaveService service, ServiceTokenManager serviceTokenManager, Slave slave, MasterConfigurationManager configurationManager, ResourceManager resourceManager)
    {
        this.service = service;
        this.serviceTokenManager = serviceTokenManager;
        this.slave = slave;
        this.configurationManager = configurationManager;
        this.resourceManager = resourceManager;
    }

    public String getUrl()
    {
        return slave.getHost() + ":" + slave.getPort();
    }

    public boolean hasResource(String resource, String version)
    {
        return resourceManager.getSlaveRepository(slave).hasResource(resource, version);
    }

    public boolean build(RecipeRequest request, BuildContext context)
    {
        MasterConfiguration appConfig = configurationManager.getAppConfig();
        SystemConfiguration systemConfig = configurationManager.getSystemConfig();
        String masterUrl = MasterAgent.constructMasterUrl(appConfig, systemConfig);

        try
        {
            return service.build(serviceTokenManager.getToken(), masterUrl, slave.getId(), request, context);
        }
        catch (RuntimeException e)
        {
            throw convertException("Unable to dispatch recipe request '" + request.getId() + "' to slave '" + slave.getName() + "'", e);
        }
    }

    public void collectResults(String project, String spec, long recipeId, boolean incremental, File outputDest, File workDest)
    {
        collect(project, spec, recipeId, incremental, true, outputDest);
        if (workDest != null)
        {
            collect(project, spec, recipeId, incremental, false, workDest);
        }
    }

    private void collect(String project, String spec, long recipeId, boolean incremental, boolean output, File destination)
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

            URL resultUrl = new URL("http", slave.getHost(), slave.getPort(), "/download?token=" + serviceTokenManager.getToken() + "&project=" + UrlEncoded.encodeString(project) + "&spec=" + UrlEncoded.encodeString(spec) + "&incremental=" + incremental + "&output=" + output + "&recipe=" + recipeId);
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

            if(!tempDir.renameTo(destination))
            {
                throw new BuildException("Unable to rename result directory to '" + destination.getAbsolutePath() + "'");
            }
        }
        catch (IOException e)
        {
            String message = e.getMessage();
            message = message.replaceAll("token=[0-9a-zA-Z]+&", "token=[scrubbed]&");
            throw new BuildException("Error downloading results from agent '" + slave.getName() + "': " + message, e);
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

    public void cleanup(String project, String spec, long recipeId, boolean incremental)
    {
        try
        {
            service.cleanupRecipe(serviceTokenManager.getToken(), project, spec, recipeId, incremental);
        }
        catch (Exception e)
        {
            LOG.warning("Failed to cleanup recipe '" + recipeId + "' on slave '" + slave.getName() + "'", e);
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
        return slave.getName();
    }

    public Slave getSlave()
    {
        return slave;
    }

    private BuildException convertException(String context, RuntimeException e)
    {
        return new BuildException(context + ": " + e.getMessage(), e);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof SlaveBuildService)
        {
            SlaveBuildService other = (SlaveBuildService) obj;
            return other.getSlave().equals(slave);
        }

        return false;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }
}
