package com.cinnamonbob.upgrade;

/**
 * The upgrade context contains information about the progress of the upgrade.
 */
public interface UpgradeContext
{
    /**
     * The build number being upgraded from.
     *
     */
    int getFromBuild();

    /**
     * The build number being upgraded to.
     *
     */
    int getToBuild();
}
