package com.cinnamonbob;

import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.model.Slave;
import com.cinnamonbob.services.SlaveService;

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
    private SlaveService service;
    private Slave slave;

    public SlaveBuildService(Slave slave, SlaveService service)
    {
        this.slave = slave;
        this.service = service;
    }

    public void build(RecipeRequest request)
    {
        // TODO set to the real master URL!
        service.build("localhost:8080", request);
    }

    public void collectResults(long recipeId, File dir)
    {
        // TODO proper error handling!
        ZipInputStream zis = null;

        try
        {
            // Pull down the result from the slave then explode to dir
            URL resultUrl = new URL("http", slave.getHost(), slave.getPort(), "/download?recipe=" + recipeId);
            URLConnection urlConnection = resultUrl.openConnection();

            // take url connection input stream and write contents to directory.
            zis = new ZipInputStream(urlConnection.getInputStream());
            FileSystemUtils.extractZip(zis, dir);
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (IOException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally
        {
            IOUtils.close(zis);
        }
    }

    public void cleanupResults(long recipeId)
    {
        service.cleanupResults(recipeId);
    }

    public boolean isAvailable()
    {
        // TODO, hmmm
        return true;
    }

    public Slave getSlave()
    {
        return slave;
    }
}
