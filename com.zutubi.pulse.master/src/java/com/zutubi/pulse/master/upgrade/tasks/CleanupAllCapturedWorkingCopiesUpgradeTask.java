package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.pulse.master.upgrade.ConfigurationAware;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.util.FileSystemUtils;

import java.io.File;

import org.apache.tools.ant.DirectoryScanner;

/**
 * CIB-2365 removes the concept of the working directory as a separately managed
 * artifact from a build.  Therefore we should go through and clean everything up.
 */
public class CleanupAllCapturedWorkingCopiesUpgradeTask extends AbstractUpgradeTask implements ConfigurationAware
{
    private MasterConfigurationManager configurationManager;

    public boolean haltOnFailure()
    {
        // a failure in this upgrade task is not fatal to the upgrade.
        return false;
    }

    public void execute() throws TaskException
    {
        File data = configurationManager.getDataDirectory();

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(data);
        scanner.setIncludes(new String[]{"projects/*/*/*/base"});
        scanner.setCaseSensitive(true);
        scanner.scan();

        for (String included : scanner.getIncludedDirectories())
        {
            File dir = new File(data, included);
            FileSystemUtils.rmdir(dir);
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
