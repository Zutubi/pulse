package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.personal.PatchArchive;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.util.logging.Logger;

import java.io.File;

/**
 */
public class ViewPersonalChangesAction extends BuildActionBase
{
    private static final Logger LOG = Logger.getLogger(ViewPersonalChangesAction.class);
    private long id;
    private PatchArchive patchArchive;
    private MasterConfigurationManager configurationManager;

    public void setId(long id)
    {
        this.id = id;
    }

    public PatchArchive getPatchArchive()
    {
        return patchArchive;
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
        File patchFile = paths.getUserPatchFile(getLoggedInUser().getId(), result.getNumber());
        if(!patchFile.exists())
        {
            addActionError("Patch file not found");
            return ERROR;
        }

        try
        {
            patchArchive = new PatchArchive(patchFile);
        }
        catch (PulseException e)
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
}
