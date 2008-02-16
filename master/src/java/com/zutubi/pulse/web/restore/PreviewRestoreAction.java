package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.SetupManager;
import com.zutubi.pulse.bootstrap.DefaultSetupManager;
import com.zutubi.pulse.restore.Archive;

import java.io.File;

/**
 *
 *
 */
public class PreviewRestoreAction extends RestoreActionSupport
{
    private Archive archive;

    public Archive getInfo()
    {
        return archive;
    }

    public String execute() throws Exception
    {
        //archive = archiveManager.previewRestore();

        return SUCCESS;
    }
}
