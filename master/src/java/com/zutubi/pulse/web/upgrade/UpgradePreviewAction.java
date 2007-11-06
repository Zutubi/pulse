package com.zutubi.pulse.web.upgrade;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.upgrade.UpgradeTask;
import com.zutubi.pulse.upgrade.UpgradeTaskGroup;

import java.util.List;

/**
 * <class-comment/>
 */
public class UpgradePreviewAction extends UpgradeActionSupport
{
    private List<UpgradeTask> upgradePreview;

    /**
     * The version that is being upgraded.
     */
    private Version targetVersion;

    /**
     * The version that the upgrade process will upgrade to.
     */
    private Version newVersion;

    private MasterConfigurationManager configurationManager;
    private List<UpgradeTaskGroup> upgradeGroupPreview;

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public List<UpgradeTask> getUpgradePreview()
    {
        return upgradePreview;
    }

    public List<UpgradeTaskGroup> getUpgradeGroupPreview()
    {
        return upgradeGroupPreview;
    }

    /**
     * Get the version being upgraded from.
     */
    public Version getTargetVersion()
    {
        return targetVersion;
    }

    /**
     * Get the version being upgraded too.
     */
    public Version getNewVersion()
    {
        return newVersion;
    }

    public String execute()
    {
        Data targetData = configurationManager.getData();

        targetVersion = targetData.getVersion();
        newVersion = Version.getVersion();

        upgradeManager.prepareUpgrade();

        upgradeGroupPreview = upgradeManager.previewUpgrade();

        upgradePreview = upgradeGroupPreview.get(0).getTasks();

        return SUCCESS;
    }

}
