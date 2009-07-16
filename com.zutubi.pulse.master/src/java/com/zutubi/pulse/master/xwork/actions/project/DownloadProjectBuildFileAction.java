package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.util.api.XMLUtils;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.types.TypeConfiguration;
import org.hsqldb.lib.StringInputStream;

import java.io.InputStream;

/**
 * An action to download the current pulse file for a project.
 */
public class DownloadProjectBuildFileAction extends ProjectActionBase
{
    private InputStream inputStream;
    private long contentLength;

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
        ProjectConfiguration projectConfig = getRequiredProject().getConfig();
        try
        {
            TypeConfiguration typeConfiguration = projectConfig.getType();
            String pulseFile = XMLUtils.prettyPrint(typeConfiguration.getPulseFile(projectConfig, null, null).getFileContent());
            inputStream = new StringInputStream(pulseFile);
            contentLength = pulseFile.length();
        }
        catch(BuildException e)
        {
            addActionError(e.getMessage());
            return ERROR;
        }

        return SUCCESS;
    }
}
