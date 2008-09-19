package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.tove.config.project.ProjectConfiguration;

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

    private ScmClientFactory scmClientFactory;

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
        ScmClient client = null;
        try
        {
            ProjectConfiguration projectConfig = getRequiredProject().getConfig();
            client = scmClientFactory.createClient(projectConfig.getScm());
            inputStream = client.retrieve(null, path, null);
            contentType = URLConnection.guessContentTypeFromName(path);
            return SUCCESS;
        }
        catch (ScmException e)
        {
            addActionError(e.getMessage());
            return ERROR;
        }
        finally
        {
            ScmClientUtils.close(client);
        }
    }

    public void setScmClientFactory(ScmClientFactory scmClientFactory)
    {
        this.scmClientFactory = scmClientFactory;
    }
}
