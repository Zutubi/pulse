package com.zutubi.pulse.web.restore;

import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.restore.BackupInfo;

import java.io.File;

/**
 *
 *
 */
public class RestorePreviewAction extends RestoreActionSupport
{
    private MasterConfigurationManager configurationManager;

    private BackupInfo backupInfo;

    public BackupInfo getInfo()
    {
        return backupInfo;
    }

    public String execute() throws Exception
    {
        Data data = configurationManager.getData();
        File restoreRoot = new File(data.getData(), "restore");

        // We should not be here unless we have a file to restore. However, we still
        // need to check that the restore root is as expected, and handle the error case
        // accordingly.

        restoreManager.prepareRestore(restoreRoot);

        backupInfo = restoreManager.previewRestore();

        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
