package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.RecipeProcessor;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An action to download the pulse file used in a specific build.
 */
public class DownloadBuildFileAction extends BuildActionBase
{
    private InputStream inputStream;
    private long contentLength;
    private MasterConfigurationManager configurationManager;

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public String getContentType()
    {
        return "application/xml";
    }

    public long getContentLength()
    {
        return contentLength;
    }

    public String execute() throws Exception
    {
        BuildResult result = getRequiredBuildResult();
        try
        {
            File file = new File(result.getAbsoluteOutputDir(configurationManager.getDataDirectory()), RecipeProcessor.PULSE_FILE);
            contentLength = file.length();
            inputStream = new FileInputStream(file);
        }
        catch(IOException e)
        {
            addActionError("I/O error opening file: " + e.getMessage());
            return ERROR;
        }

        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
