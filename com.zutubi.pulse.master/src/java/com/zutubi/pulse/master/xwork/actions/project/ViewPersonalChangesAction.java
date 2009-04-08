package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.scm.api.FileStatus;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.scm.ScmClientUtils;
import com.zutubi.pulse.master.scm.ScmManager;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.List;

/**
 */
public class ViewPersonalChangesAction extends BuildActionBase
{
    private static final Logger LOG = Logger.getLogger(ViewPersonalChangesAction.class);
    private long id;
    private List<FileStatus> fileStatuses;
    private MasterConfigurationManager configurationManager;
    private ScmManager scmManager;

    public void setId(long id)
    {
        this.id = id;
    }

    public List<FileStatus> getFileStatuses()
    {
        return fileStatuses;
    }

    public String execute()
    {
        BuildResult result = getRequiredBuildResult();
        if(!result.isPersonal())
        {
            addActionError("Build [" + id + "] is not a personal build");
            return ERROR;
        }

        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        final File patchFile = paths.getUserPatchFile(getLoggedInUser().getId(), result.getNumber());
        if(!patchFile.exists())
        {
            addActionError("Patch file not found");
            return ERROR;
        }

        try
        {
            fileStatuses = ScmClientUtils.withScmClient(result.getProject().getConfig(), scmManager, new ScmClientUtils.ScmContextualAction<List<FileStatus>>()
            {
                public List<FileStatus> process(ScmClient client, ScmContext context) throws ScmException
                {
                    return client.readFileStatuses(context, patchFile);
                }
            });
        }
        catch (ScmException e)
        {
            LOG.warning(e);
            addActionError(e.getMessage());
            return ERROR;
        }

        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setScmManager(ScmManager scmManager)
    {
        this.scmManager = scmManager;
    }
}
