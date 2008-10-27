package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

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

    private ScmManager scmManager;

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
            ScmConfiguration scmConfig = projectConfig.getScm();
            if (scmManager.isReady(scmConfig))
            {
                client = scmManager.createClient(scmConfig);
                ScmContext context = scmManager.createContext(projectConfig.getProjectId(), scmConfig);
                inputStream = client.retrieve(context, path, null);
                contentType = URLConnection.guessContentTypeFromName(path);
                return SUCCESS;
            }
            else
            {
                throw new ScmException("scm is not ready");
            }
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

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
