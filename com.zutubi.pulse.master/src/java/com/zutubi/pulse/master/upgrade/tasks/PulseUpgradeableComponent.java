package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.master.bootstrap.Data;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.upgrade.ConfigurationAware;
import com.zutubi.pulse.master.upgrade.UpgradeTask;
import com.zutubi.pulse.master.upgrade.UpgradeableComponent;
import com.zutubi.pulse.master.util.monitor.JobListener;
import com.zutubi.pulse.master.util.monitor.TaskFeedback;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Handles upgrades built in to Pulse (not plugged in).
 */
public class PulseUpgradeableComponent implements UpgradeableComponent, JobListener<PulseUpgradeTask>
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
     * @param toBuildNumber   specifies the upper build number and is included in the determination.
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
        Version from = getDataVersion();
        Version to = Version.getVersion();

        return isUpgradeRequired(from.getBuildNumberAsInt(), to.getBuildNumberAsInt());
    }

    private Version getDataVersion()
    {
        return configurationManager.getData().getVersion();
    }

    public List<UpgradeTask> getUpgradeTasks()
    {
        Version from = getDataVersion();
        Version to = Version.getVersion();

        List<PulseUpgradeTask> tasks = new LinkedList<PulseUpgradeTask>();

        List<PulseUpgradeTask> requiredUpgradeTasks = determineRequiredUpgradeTasks(from.getBuildNumberAsInt(), to.getBuildNumberAsInt());
        tasks.addAll(requiredUpgradeTasks);

        //HAXORZ: need do some manual wiring here since the timing of the startup is awkward.
        for (UpgradeTask task : tasks)
        {
            if (ConfigurationAware.class.isAssignableFrom(task.getClass()))
            {
                ((ConfigurationAware) task).setConfigurationManager(configurationManager);
            }
        }

        tasks.addAll(systemTasks);

        Collections.sort(tasks, new PulseUpgradeTaskComparator());

        return new LinkedList<UpgradeTask>(tasks);
    }

    public void upgradeStarted()
    {
        upgradeTarget = configurationManager.getData();
    }

    public void upgradeCompleted()
    {
        // assumes a successful upgrade.
        upgradeTarget.updateVersion(Version.getVersion());
    }

    public void upgradeAborted()
    {

    }

    //--- JobListener implementation --- 

    public void taskCompleted(PulseUpgradeTask task, TaskFeedback<PulseUpgradeTask> feedback)
    {
        // record task completion, to ensure that it is not run a second time. Any task with a build
        // number less than zero are run during each upgrade and do not impact the target build number.
        if (task.getBuildNumber() > 0)
        {
            upgradeTarget.setBuildNumber(task.getBuildNumber());
        }
    }

    public void taskFailed(PulseUpgradeTask task, TaskFeedback<PulseUpgradeTask> feedback)
    {
        // noop
    }

    public void taskAborted(PulseUpgradeTask task, TaskFeedback feedback)
    {
        // noop
    }

    public void taskStarted(PulseUpgradeTask task, TaskFeedback<PulseUpgradeTask> feedback)
    {
        // noop
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
