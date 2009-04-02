package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.BootstrapperSupport;
import com.zutubi.pulse.core.commands.api.CommandContext;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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

    public void bootstrap(CommandContext commandContext) throws BuildException
    {
        try
        {
            // Pull down the result from the slave then explode to dir
            URL resultUrl = new URL(url + "/download?token=" + token + "&output=false&recipe=" + previousRecipeId);
            URLConnection urlConnection = resultUrl.openConnection();

            // take url connection input stream and write contents to directory.
            FileOutputStream zos = null;
            ExecutionContext context = commandContext.getExecutionContext();
            File zipName = new File(context.getWorkingDir().getAbsolutePath() + ".zip");

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

            PulseZipUtils.extractZip(zipName, context.getWorkingDir());
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
