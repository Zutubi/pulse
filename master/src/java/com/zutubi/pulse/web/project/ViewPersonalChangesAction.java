package com.zutubi.pulse.web.project;

import com.zutubi.pulse.MasterBuildPaths;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.personal.PatchArchive;
import com.zutubi.util.logging.Logger;

import java.io.File;

/**
 */
public class ViewPersonalChangesAction extends ProjectActionSupport
{
    private static final Logger LOG = Logger.getLogger(ViewPersonalChangesAction.class);
    private long id;
    private BuildResult result;
    private PatchArchive patchArchive;
    private MasterConfigurationManager configurationManager;

    public void setId(long id)
    {
        this.id = id;
    }

    public BuildResult getResult()
    {
        return result;
    }

    public PatchArchive getPatchArchive()
    {
        return patchArchive;
    }

    public String execute()
    {
        result = getBuildManager().getBuildResult(id);
        if(result == null)
        {
            addActionError("Unknown build [" + id + "]");
            return ERROR;
        }

        if(!result.isPersonal())
        {
            addActionError("Build [" + id + "] is not a personal build");
            return ERROR;
        }

        checkPermissions(result);

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
