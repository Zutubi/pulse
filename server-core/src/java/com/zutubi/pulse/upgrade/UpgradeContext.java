/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.Home;

import java.util.List;

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

    List<UpgradeTask> getTasks();

    Home getHome();

    Version getFrom();

    Version getTo();
}
