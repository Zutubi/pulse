package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.util.api.XMLUtils;
import com.zutubi.pulse.master.scm.ScmFileResolver;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import org.hsqldb.lib.StringInputStream;

import java.io.InputStream;

/**
 * An action to download the current pulse file for a project.
 */
public class DownloadProjectBuildFileAction extends ProjectActionBase
{
    private InputStream inputStream;
    private long contentLength;
    private ScmManager scmManager;

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
            ScmFileResolver resolver = new ScmFileResolver(projectConfig, Revision.HEAD, scmManager);
            String pulseFile = XMLUtils.prettyPrint(projectConfig.getType().getPulseFile().getFileContent(resolver));
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

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
