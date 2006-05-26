/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.Data;

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

    Data getData();

    Version getFrom();

    Version getTo();
}
