package com.zutubi.pulse;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.pulse.core.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipInputStream;

/**
 */
public class CopyBootstrapper extends BootstrapperSupport
{
    private String url;
    private long previousRecipeId;

    public CopyBootstrapper(String url, long previousRecipeId)
    {
        this.url = url;
        this.previousRecipeId = previousRecipeId;
    }

    public void bootstrap(long recipeId, RecipePaths paths) throws BuildException
    {
        try
        {
            // Pull down the result from the slave then explode to dir
            URL resultUrl = new URL("http://" + url + "/download?output=false&recipe=" + previousRecipeId);
            URLConnection urlConnection = resultUrl.openConnection();

            // take url connection input stream and write contents to directory.
            FileOutputStream zos = null;
            File zipName = new File(paths.getBaseDir().getAbsolutePath() + ".zip");

            try
            {
                zos = new FileOutputStream(zipName);
                IOUtils.joinStreams(urlConnection.getInputStream(), zos);
            }
            finally
            {
                IOUtils.close(urlConnection.getInputStream());
                IOUtils.close(zos);
            }

            ZipInputStream zis = null;

            try
            {
                zis = new ZipInputStream(new FileInputStream(zipName));
                FileSystemUtils.extractZip(zis, paths.getBaseDir());
            }
            finally
            {
                IOUtils.close(zis);
            }
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
    }
}
