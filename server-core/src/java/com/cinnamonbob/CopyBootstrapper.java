package com.cinnamonbob;

import com.cinnamonbob.core.Bootstrapper;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.RecipePaths;
import com.cinnamonbob.core.util.FileSystemUtils;
import com.cinnamonbob.core.util.IOUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipInputStream;

/**
 */
public class CopyBootstrapper implements Bootstrapper
{
    private String url;
    private long recipeId;

    public CopyBootstrapper(String url, long recipeId)
    {
        this.url = url;
        this.recipeId = recipeId;
    }

    public void bootstrap(RecipePaths paths) throws BuildException
    {
        ZipInputStream zis = null;

        try
        {
            // Pull down the result from the slave then explode to dir
            URL resultUrl = new URL("http://" + url + "/download?output=false&recipe=" + recipeId);
            URLConnection urlConnection = resultUrl.openConnection();

            // take url connection input stream and write contents to directory.
            zis = new ZipInputStream(urlConnection.getInputStream());
            FileSystemUtils.extractZip(zis, paths.getWorkDir());
        }
        catch (MalformedURLException e)
        {
            // Programmer error
            e.printStackTrace();
        }
        catch (IOException e)
        {
            throw new BuildException("Bootstrapping working directory from '" + url + "': " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(zis);
        }
    }
}
