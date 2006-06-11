package com.zutubi.pulse;

import com.caucho.hessian.client.HessianRuntimeException;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.RecipeRequest;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.services.ServiceTokenManager;
import com.zutubi.pulse.services.InvalidTokenException;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipInputStream;

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

    public boolean build(RecipeRequest request)
    {
        try
        {
            return service.build(serviceTokenManager.getToken(), configurationManager.getAppConfig().getHostName(), slave.getId(), request);
        }
        catch (RuntimeException e)
        {
            throw convertException("Unable to dispatch recipe request '" + request.getId() + "' to slave '" + slave.getName() + "'", e);
        }
    }

    public void collectResults(long recipeId, File outputDest, File workDest)
    {
        collect(recipeId, true, outputDest);
        collect(recipeId, false, workDest);
    }

    private void collect(long recipeId, boolean output, File destination)
    {
        ZipInputStream zis = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try
        {
            URL resultUrl = new URL("http", slave.getHost(), slave.getPort(), "/download?output=" + output + "&recipe=" + recipeId);
            URLConnection urlConnection = resultUrl.openConnection();

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
            fis = new FileInputStream(zipFile);
            zis = new ZipInputStream(fis);
            FileSystemUtils.extractZip(zis, destination);
            IOUtils.close(fis);
            fis = null;
            IOUtils.close(zis);
            zis = null;

            zipFile.delete();
        }
        catch (IOException e)
        {
            throw new BuildException("Error downloading results from agent '" + slave.getName() + ": " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(zis);
            IOUtils.close(fis);
            IOUtils.close(fos);
        }
    }

    public void cleanup(long recipeId)
    {
        try
        {
            service.cleanupRecipe(serviceTokenManager.getToken(), recipeId);
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
