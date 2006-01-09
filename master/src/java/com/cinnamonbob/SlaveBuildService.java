package com.cinnamonbob;

import com.caucho.hessian.client.HessianRuntimeException;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.model.Slave;
import com.cinnamonbob.services.SlaveService;
import com.cinnamonbob.util.logging.Logger;

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
            service.build(BobServer.getHostURL(), request);
        }
        catch (HessianRuntimeException e)
        {
            throw convertException("Unable to dispatch recipe request '" + request.getId() + "' to slave '" + slave.getName() + "'", e);
        }
    }

    public void collectResults(long recipeId, File dir)
    {
        ZipInputStream zis = null;

        try
        {
            // Pull down the result from the slave then explode to dir
            URL resultUrl = new URL("http", slave.getHost(), slave.getPort(), "/download?output=true&recipe=" + recipeId);
            URLConnection urlConnection = resultUrl.openConnection();

            // take url connection input stream and write contents to directory.
            zis = new ZipInputStream(urlConnection.getInputStream());
            FileSystemUtils.extractZip(zis, dir);
        }
        catch (MalformedURLException e)
        {
            // Programmer error
            e.printStackTrace();
        }
        catch (IOException e)
        {
            throw new BuildException("Downloading results from slave '" + slave.getName() + ": " + e.getMessage(), e);
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
        if(obj instanceof SlaveBuildService)
        {
            SlaveBuildService other = (SlaveBuildService) obj;
            return other.getSlave().equals(slave);
        }

        return false;
    }
}
