package com.zutubi.pulse.web.project;

import com.zutubi.pulse.prototype.config.ProjectConfiguration;
import com.zutubi.pulse.scm.SCMException;

import java.io.InputStream;
import java.net.URLConnection;

/**
 * An action to stream a file from an SCM server to the client.
 */
// FIXME: is this used? if so, the params will have changed...
public class DownloadSCMFileAction extends ProjectActionBase
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
            ProjectConfiguration projectConfig = getProjectConfig();
            inputStream = projectConfig.getScm().createClient().checkout(null, path);
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
