package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.scm.SCMException;

import java.io.InputStream;
import java.net.URLConnection;

/**
 * An action to stream a file from an SCM server to the client.
 */
public class DownloadSCMFileAction extends ProjectActionSupport
{
    private String path;
    private InputStream inputStream;
    private String contentType;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String execute()
    {
        try
        {
            Project project = lookupProject(projectId);
            if (project == null)
            {
                return ERROR;
            }

            inputStream = project.getScm().createServer().checkout(null, path);
            contentType = URLConnection.guessContentTypeFromName(path);
            return SUCCESS;
        }
        catch (SCMException e)
        {
            addActionError(e.getMessage());
            return ERROR;
        }
    }
}
