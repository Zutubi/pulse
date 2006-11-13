package com.zutubi.pulse.web.project;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.model.BuildResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An action to download the pulse file used in a specific build.
 */
public class DownloadBuildFileAction extends ProjectActionSupport
{
    private long id;
    private InputStream inputStream;
    private long contentLength;
    private MasterConfigurationManager configurationManager;

    public void setId(long id)
    {
        this.id = id;
    }

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
        BuildResult result = getBuildManager().getBuildResult(id);
        if(result == null)
        {
            addActionError("Unknown build [" + id + "]");
            return ERROR;
        }

        checkPermissions(result);        

        try
        {
            File file = new File(result.getAbsoluteOutputDir(configurationManager.getDataDirectory()), BuildResult.PULSE_FILE);
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
