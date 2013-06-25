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
     * Indicates whether any upgradeable component in this soure requires an upgrade to move between the given builds.
     *
     * @param fromBuildNumber the original build number
     * @param toBuildNumber the target build number
     * @return true if an upgrade is required, false otherwise.
     */
    boolean isUpgradeRequired(int fromBuildNumber, int toBuildNumber);

    /**
     * Return the list of upgradeable components that require upgrading.
     * @return a list of upgrade components.
     */
    List<UpgradeableComponent> getUpgradeableComponents();
}
