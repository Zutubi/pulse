package com.zutubi.pulse.web.project;

import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.util.XMLUtils;
import com.zutubi.pulse.prototype.config.ProjectConfiguration;
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
        Project project = getProject();
        ProjectConfiguration projectConfig = getProjectConfig();

        try
        {
            PulseFileDetails pulseFileDetails = project.getPulseFileDetails();
            ComponentContext.autowire(pulseFileDetails);
            String pulseFile = XMLUtils.prettyPrint(pulseFileDetails.getPulseFile(0, projectConfig, project, null, null));
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
