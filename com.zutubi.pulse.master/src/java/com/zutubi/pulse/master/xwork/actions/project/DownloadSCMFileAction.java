package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.scm.ScmManager;

import java.io.InputStream;
import java.net.URLConnection;

import static com.zutubi.pulse.master.scm.ScmClientUtils.ScmContextualAction;
import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;

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
        try
        {
            Project project = getRequiredProject();
            return withScmClient(project.getConfig(), project.getState(), scmManager, new ScmContextualAction<String>()
            {
                public String process(ScmClient client, ScmContext context) throws ScmException
                {
                    inputStream = client.retrieve(context, path, null);
                    contentType = URLConnection.guessContentTypeFromName(path);
                    return SUCCESS;
                }
            });

        }
        catch (ScmException e)
        {
            addActionError(e.getMessage());
            return ERROR;
        }
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
