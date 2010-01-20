package com.zutubi.pulse.master.upgrade;

import java.util.List;

/**
 * The UpgradeableComponentSource is a factory for upgradeable components.
 */
public interface UpgradeableComponentSource
{
    /**
     * Returns true if this source has some components that require upgrading.
     * @return true if an upgrade is required
     */
    boolean isUpgradeRequired();

    /**
     * Return the list of upgradeable components that require upgrading.
     * @return a list of upgrade components.
     */
    List<UpgradeableComponent> getUpgradeableComponents();
}
