package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.bootstrap.Data;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.upgrade.ConfigurationAware;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.pulse.servercore.cleanup.FileDeletionService;

import java.io.File;

/**
 * Cleanup the existing ivy cache directories.  We have disabled caching so be
 * remain consistent we now cleanup any existing cache entries.
 */
public class CleanIvyCacheUpgradeTask extends AbstractUpgradeTask implements ConfigurationAware
{
    private MasterConfigurationManager configurationManager;
    private FileDeletionService fileDeletionService;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        Data data = configurationManager.getData();
        File cacheBase = new File(data.getData(), "cache");

        // this could take a while so schedule it in the background.
        fileDeletionService.delete(cacheBase, false);
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public void setFileDeletionService(FileDeletionService fileDeletionService)
    {
        this.fileDeletionService = fileDeletionService;
    }
}
