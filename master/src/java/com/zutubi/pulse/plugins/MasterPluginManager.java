package com.zutubi.pulse.plugins;

import com.zutubi.pulse.upgrade.UpgradeableComponent;
import com.zutubi.pulse.upgrade.UpgradeableComponentSource;

import java.util.List;

/**
 *
 *
 */
public class MasterPluginManager extends PluginManager implements UpgradeableComponentSource
{
    public boolean isUpgradeRequired()
    {
        return true;
    }

    public List<UpgradeableComponent> getUpgradeableComponents()
    {
        return null;
    }
}
