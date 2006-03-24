package com.cinnamonbob.web.upgrade;

import com.cinnamonbob.Version;
import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.bootstrap.Home;
import com.cinnamonbob.upgrade.UpgradeTask;

import java.util.List;

/**
 * <class-comment/>
 */
public class UpgradePreviewAction extends UpgradeActionSupport
{
    private List<UpgradeTask> upgradePreview;

    private Version targetVersion;

    private Home targetHome;

    private ConfigurationManager configurationManager;

    /**
     * Required resource.
     *
     * @param configurationManager
     */
    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    public List<UpgradeTask> getUpgradePreview()
    {
        return upgradePreview;
    }

    public Version getTargetVersion()
    {
        return targetVersion;
    }

    public String execute()
    {
        targetHome = configurationManager.getHome();
        targetVersion = targetHome.getHomeVersion();

        upgradeManager.prepareUpgrade(targetHome);

        upgradePreview = upgradeManager.previewUpgrade();
        return SUCCESS;
    }

}
