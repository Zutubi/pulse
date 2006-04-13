/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse;

import com.caucho.hessian.client.HessianRuntimeException;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.pulse.core.util.IOUtils;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.bootstrap.ConfigurationManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
    private ConfigurationManager configurationManager;

    public SlaveBuildService(Slave slave, SlaveService service)
    {
        this.slave = slave;
        this.service = service;
    }

    public String getUrl()
    {
        return slave.getHost() + ":" + slave.getPort();
    }

    public void build(RecipeRequest request)
    {
        try
        {
            service.build(configurationManager.getAppConfig().getHostName(), request);
        }
        catch (HessianRuntimeException e)
        {
            throw convertException("Unable to dispatch recipe request '" + request.getId() + "' to slave '" + slave.getName() + "'", e);
        }
    }

    public void collectResults(long recipeId, File outputDest, File workDest)
    {
        ZipInputStream zis = null;

        try
        {
            // Pull down the result from the slave then explode to dir
            URL resultUrl = new URL("http", slave.getHost(), slave.getPort(), "/download?output=true&recipe=" + recipeId);
            URLConnection urlConnection = resultUrl.openConnection();

            // take url connection input stream and write contents to directory.
            zis = new ZipInputStream(urlConnection.getInputStream());
            FileSystemUtils.extractZip(zis, outputDest);
            IOUtils.close(zis);
            zis = null;

            resultUrl = new URL("http", slave.getHost(), slave.getPort(), "/download?output=false&recipe=" + recipeId);
            urlConnection = resultUrl.openConnection();
            zis = new ZipInputStream(urlConnection.getInputStream());
            FileSystemUtils.extractZip(zis, workDest);
        }
        catch (MalformedURLException e)
        {
            // Programmer error
            e.printStackTrace();
        }
        catch (IOException e)
        {
            throw new BuildException("Error downloading results from slave '" + slave.getName() + ": " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(zis);
        }
    }

    public void cleanup(long recipeId)
    {
        try
        {
            service.cleanupRecipe(recipeId);
        }
        catch (Exception e)
        {
            LOG.warning("Failed to cleanup recipe '" + recipeId + "' on slave '" + slave.getName() + "'", e);
        }
    }

    public void terminateRecipe(long recipeId)
    {
        service.terminateRecipe(recipeId);
    }

    public String getHostName()
    {
        return slave.getName();
    }

    public Slave getSlave()
    {
        return slave;
    }

    private BuildException convertException(String context, HessianRuntimeException e)
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

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
