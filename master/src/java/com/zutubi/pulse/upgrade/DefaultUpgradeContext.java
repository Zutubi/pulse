package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.Data;

import java.util.List;

/**
 * The upgrade context object contains information relating to the upgrade that is
 * being executed. In particular, this includes the versions, tasks and data directory.
 *
 * @author Daniel Ostermeier
 */
public class DefaultUpgradeContext implements UpgradeContext
{
    /**
     * The version from which we are upgrading.
     */
    private Version from;

    /**
     * The version to which we are upgrading.
     */
    private Version to;

    /**
     * The list of upgrade tasks that will be executed during this upgrade.
     */
    private List<UpgradeTask> tasks = null;

    /**
     * The data directory that is being upgraded.
     */
    private Data data;

    public DefaultUpgradeContext(Version from, Version to)
    {
        this.from = from;
        this.to = to;
    }

    /**
     * Get the version we are upgrading from. This is typically the version of the
     * target data directory.
     *
     * @return version
     */
    public Version getFrom()
    {
        return from;
    }

    /**
     * Retrieve the version we are upgrading to. This is typically the version of the
     * software installation.
     *
     * @return version
     */
    public Version getTo()
    {
        return to;
    }

    /**
     * Retrieve the build number of for the version we are upgrading from. If this build
     * number is invalid, we return Version.INVALID
     *
     * @return build number
     */
    public int getFromBuild()
    {
        return from.getBuildNumberAsInt();
    }

    /**
     * Retrieve the build number of for the version we are upgrading to. If this build
     * number is invalid, we return Version.INVALID
     *
     * @return build number
     */
    public int getToBuild()
    {
        return to.getBuildNumberAsInt();
    }

    public void setTasks(List<UpgradeTask> tasks)
    {
        this.tasks = tasks;
    }

    /**
     * Get the list of upgrade tasks that are going to be executed
     * as part of this upgrade.
     *
     * @return a list of upgarde tasks.
     */
    public List<UpgradeTask> getTasks()
    {
        return tasks;
    }

    /**
     * Get the data directory that is being upgraded.
     *
     * @return data directory.
     */
    public Data getData()
    {
        return data;
    }

    public void setData(Data data)
    {
        this.data = data;
    }
}
