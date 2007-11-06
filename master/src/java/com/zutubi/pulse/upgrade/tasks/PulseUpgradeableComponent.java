package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.upgrade.ConfigurationAware;
import com.zutubi.pulse.upgrade.UpgradeTask;
import com.zutubi.pulse.upgrade.UpgradeableComponent;
import com.zutubi.pulse.upgrade.UpgradeListener;

import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Arrays;

/**
 *
 *
 */
public class PulseUpgradeableComponent implements UpgradeableComponent, UpgradeListener
{
    /**
     * The registered upgrade tasks.
     */
    private List<PulseUpgradeTask> upgradeTasks = new LinkedList<PulseUpgradeTask>();

    private List<PulseUpgradeTask> systemTasks = new LinkedList<PulseUpgradeTask>();

    private MasterConfigurationManager configurationManager;
    private Data upgradeTarget;

    /**
     * Register an upgrade task with this upgrade manager.
     *
     * @param task to be registered
     */
    public void addTask(PulseUpgradeTask task)
    {
        upgradeTasks.add(task);
    }

    /**
     * Set the full list of upgrade tasks. This new set of tasks will override any existing
     * registered tasks.
     *
     * @param tasks a list of upgrade task instances
     */
    public void setTasks(List<PulseUpgradeTask> tasks)
    {
        this.upgradeTasks = tasks;
    }

    /**
     * Set the full list of upgrade tasks. This new set of tasks will override any existing
     * registered tasks.
     *
     * @param tasks a list of upgrade task instances
     */
    public void setTasks(PulseUpgradeTask... tasks)
    {
        this.upgradeTasks = Arrays.asList(tasks);
    }

    /**
     * Set the full list of system upgrade tasks. These tasks are run during every upgrade.
     *
     * @param tasks a list of upgrade task instances
     */
    public void setSystemTasks(List<PulseUpgradeTask> tasks)
    {
        this.systemTasks = tasks;
    }

    /**
     * Determine if an upgrade is required between the specified build numbers.
     *
     * @param fromBuildNumber specifies the lower build number and is not included in the determination.
     * @param toBuildNumber   specifiea the upper build number and is included in the determination.
     * @return true if an upgrade is required, false otherwise.
     */
    public boolean isUpgradeRequired(int fromBuildNumber, int toBuildNumber)
    {
        // If we do not understand what version we are upgrading to, we assume that the build number
        // if the latest available, and hence need to upgrade. If our guess is incorrect, then no
        // actual upgrade tasks will be executed, so there is no harm. When is this likely to occur?
        // During development, thats when.
        boolean required;
        required = toBuildNumber == Version.INVALID || fromBuildNumber < toBuildNumber;

        return required && determineRequiredUpgradeTasks(fromBuildNumber, toBuildNumber).size() > 0;
    }

    /**
     * Determine which upgrade tasks need to be executed during an upgrade between the
     * indicated versions.
     *
     * @param fromBuildNumber specifies the lower build number and is not included in the determination.
     * @param toBuildNumber   specifiea the upper build number and is included in the determination.
     * @return a list of upgrade tasks that are required.
     */
    protected List<PulseUpgradeTask> determineRequiredUpgradeTasks(int fromBuildNumber, int toBuildNumber)
    {
        List<PulseUpgradeTask> requiredTasks = new LinkedList<PulseUpgradeTask>();

        // do not attempt an upgrade if we do not know the version of the data being upgraded.
        if (fromBuildNumber == Version.INVALID)
        {
            return requiredTasks;
        }

        for (PulseUpgradeTask task : upgradeTasks)
        {
            if (fromBuildNumber < task.getBuildNumber())
            {
                if (toBuildNumber == Version.INVALID || task.getBuildNumber() <= toBuildNumber)
                {
                    requiredTasks.add(task);
                }
            }
        }

        Collections.sort(requiredTasks, new PulseUpgradeTaskComparator());
        return requiredTasks;
    }

    public boolean isUpgradeRequired()
    {
        Version from = configurationManager.getData().getVersion();
        Version to = Version.getVersion();

        return isUpgradeRequired(from.getBuildNumberAsInt(), to.getBuildNumberAsInt());
    }

    public List<UpgradeTask> getUpgradeTasks()
    {
        Version from = configurationManager.getData().getVersion();
        Version to = Version.getVersion();

        List<PulseUpgradeTask> tasks = new LinkedList<PulseUpgradeTask>();
        tasks.addAll(systemTasks);
        tasks.addAll(determineRequiredUpgradeTasks(from.getBuildNumberAsInt(), to.getBuildNumberAsInt()));


        //HAXORZ: need do some manual wiring here since the timing of the startup is awkward.
        for (UpgradeTask task : tasks)
        {
            if (ConfigurationAware.class.isAssignableFrom(task.getClass()))
            {
                ((ConfigurationAware) task).setConfigurationManager(configurationManager);
            }
        }

        Collections.sort(tasks, new PulseUpgradeTaskComparator());

        return new LinkedList<UpgradeTask>(tasks);
    }

    public void prepareUpgrade()
    {
        upgradeTarget = configurationManager.getData();
    }

    public void completeUpgrade()
    {
        // assumes a successful upgrade.
        upgradeTarget.updateVersion(Version.getVersion());
    }

    public void taskComplete(UpgradeTask task)
    {
        // record task completion, to ensure that it is not run a second time. Any task with a build
        // number less than zero are run during each upgrade and do not impact the target build number.

        PulseUpgradeTask pulseTask = (PulseUpgradeTask) task;

        if (pulseTask.getBuildNumber() > 0)
        {
            upgradeTarget.setBuildNumber(pulseTask.getBuildNumber());
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
