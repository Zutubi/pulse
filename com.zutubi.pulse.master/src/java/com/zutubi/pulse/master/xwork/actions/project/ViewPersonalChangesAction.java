package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.patch.PatchFormatFactory;
import com.zutubi.pulse.core.scm.patch.PatchProperties;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;
import com.zutubi.pulse.core.scm.patch.api.PatchFormat;
import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeViewerConfiguration;
import com.zutubi.util.logging.Logger;

import java.io.File;
import java.util.List;

/**
 */
public class ViewPersonalChangesAction extends BuildActionBase
{
    private static final Logger LOG = Logger.getLogger(ViewPersonalChangesAction.class);
    private long id;
    private String changeUrl;
    private List<FileStatus> fileStatuses;
    private MasterConfigurationManager configurationManager;
    private PatchFormatFactory patchFormatFactory;

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

        if (result.getRevision() != null)
        {
            ChangeViewerConfiguration changeViewer = result.getProject().getConfig().getChangeViewer();
            if (changeViewer != null)
            {
                changeUrl = changeViewer.getRevisionURL(result.getRevision());
            }
        }

        User loggedInUser = getLoggedInUser();
        MasterBuildPaths paths = new MasterBuildPaths(configurationManager);
        final File patchFile = paths.getUserPatchFile(loggedInUser.getId(), result.getNumber());
        if(!patchFile.exists())
        {
            addActionError("Patch file not found");
            return ERROR;
        }

        File propertiesFile = paths.getUserPatchPropertiesFile(loggedInUser.getId(), result.getNumber());
        PatchProperties patchProperties = new PatchProperties(propertiesFile);
        String patchFormatType = patchProperties.getPatchFormat();
        PatchFormat format = patchFormatFactory.createByFormatType(patchFormatType);

        try
        {
            fileStatuses = format.readFileStatuses(patchFile);
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

    public void setPatchFormatFactory(PatchFormatFactory patchFormatFactory)
    {
        this.patchFormatFactory = patchFormatFactory;
    }
}
