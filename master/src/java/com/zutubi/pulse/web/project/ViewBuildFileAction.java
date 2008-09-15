package com.zutubi.pulse.web.project;

import com.uwyn.jhighlight.renderer.XmlXhtmlRenderer;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.util.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * An action to show the build file used for a build.  The file is
 * highlighted beautifully for the benefit of the user.
 */
public class ViewBuildFileAction extends BuildActionBase
{
    private String highlightedFile;
    private MasterConfigurationManager configurationManager;

    public String getHighlightedFile()
    {
        return highlightedFile;
    }

    public String execute()
    {
        BuildResult result = getRequiredBuildResult();
        FileInputStream is = null;
        ByteArrayOutputStream os = null;

        try
        {
            is = new FileInputStream(new File(result.getAbsoluteOutputDir(configurationManager.getDataDirectory()), BuildResult.PULSE_FILE));
            os = new ByteArrayOutputStream();
            XmlXhtmlRenderer renderer = new XmlXhtmlRenderer();
            renderer.highlight(BuildResult.PULSE_FILE, is, os, null, true);
            highlightedFile = os.toString();
        }
        catch (IOException e)
        {
            // Ignore.
        }
        finally
        {
            IOUtils.close(is);
            IOUtils.close(os);
        }

        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
