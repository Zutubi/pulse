/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.upgrade;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.bootstrap.Home;
import com.zutubi.pulse.upgrade.UpgradeTask;

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
        targetVersion = targetHome.getVersion();

        upgradeManager.prepareUpgrade(targetHome);

        upgradePreview = upgradeManager.previewUpgrade();
        return SUCCESS;
    }

}
