package com.zutubi.pulse;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.CommandContext;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;

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
    private String token;

    public CopyBootstrapper(String url, String token, long previousRecipeId)
    {
        this.url = url;
        this.token = token;
        this.previousRecipeId = previousRecipeId;
    }

    public void bootstrap(CommandContext context) throws BuildException
    {
        try
        {
            // Pull down the result from the slave then explode to dir
            URL resultUrl = new URL("http://" + url + "/download?token=" + token + "&output=false&recipe=" + previousRecipeId);
            URLConnection urlConnection = resultUrl.openConnection();

            // take url connection input stream and write contents to directory.
            FileOutputStream zos = null;
            File zipName = new File(context.getPaths().getBaseDir().getAbsolutePath() + ".zip");

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
                FileSystemUtils.extractZip(zis, context.getPaths().getBaseDir());
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
