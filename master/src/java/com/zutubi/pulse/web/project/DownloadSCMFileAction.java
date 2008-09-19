package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.ScmContext;
import com.zutubi.pulse.core.scm.ScmContextFactory;
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
    private ScmContextFactory scmContextFactory;

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
            ScmContext context = scmContextFactory.createContext(projectConfig.getProjectId(), projectConfig.getScm());
            inputStream = client.retrieve(context, path, null);
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

    public void setScmContextFactory(ScmContextFactory scmContextFactory)
    {
        this.scmContextFactory = scmContextFactory;
    }
}
