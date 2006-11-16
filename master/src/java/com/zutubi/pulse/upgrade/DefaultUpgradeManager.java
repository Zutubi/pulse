package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.util.logging.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultUpgradeManager implements UpgradeManager
{
    private static final Logger LOG = Logger.getLogger(DefaultUpgradeManager.class);

    /**
     * The registered upgrade tasks.
     */
    private List<UpgradeTask> upgradeTasks = new LinkedList<UpgradeTask>();

    private List<UpgradeTask> systemTasks = new LinkedList<UpgradeTask>();

    /**
     * The upgrade context for the current upgrade. This will be null if no upgrade
     * is in progress / prepared.
     */
    private DefaultUpgradeContext currentContext;

    private UpgradeProgressMonitor monitor;

    private Data upgradeTarget;

    private MasterConfigurationManager configurationManager;

    /**
     * Register an upgrade task with this upgrade manager.
     *
     * @param task to be registered
     */
    public void addTask(UpgradeTask task)
    {
        upgradeTasks.add(task);
    }

    /**
     * Set the full list of upgrade tasks. This new set of tasks will override any existing
     * registered tasks.
     *
     * @param tasks a list of upgrade task instances
     */
    public void setTasks(List<UpgradeTask> tasks)
    {
        this.upgradeTasks = tasks;
    }

    /**
     * Set the full list of upgrade tasks. This new set of tasks will override any existing
     * registered tasks.
     *
     * @param tasks a list of upgrade task instances
     */
    public void setTasks(UpgradeTask... tasks)
    {
        this.upgradeTasks = Arrays.asList(tasks);
    }

    /**
     * Set the full list of system upgrade tasks. These tasks are run during every upgrade.
     * 
     * @param tasks a list of upgrade task instances
     */
    public void setSystemTasks(List<UpgradeTask> tasks)
    {
        this.systemTasks = tasks;
    }

    /**
     * Determine if an upgrade is required between the specified build numbers.
     *
     * @param fromBuildNumber specifies the lower build number and is not included in the determination.
     * @param toBuildNumber specifiea the upper build number and is included in the determination.
     *
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

    public List<UpgradeTask> previewUpgrade(int fromBuildNumber, int toBuildNumber)
    {
        return compileUpgradeTasks(fromBuildNumber, toBuildNumber);
    }

    /**
     * Determine which upgrade tasks need to be executed during an upgrade between the
     * indicated versions.
     *
     * @param fromBuildNumber specifies the lower build number and is not included in the determination.
     * @param toBuildNumber specifiea the upper build number and is included in the determination.
     *
     * @return a list of upgrade tasks that are required.
     */
    protected List<UpgradeTask> determineRequiredUpgradeTasks(int fromBuildNumber, int toBuildNumber)
    {
        List<UpgradeTask> requiredTasks = new LinkedList<UpgradeTask>();

        // do not attempt an upgrade if we do not know the version of the data being upgraded.
        if (fromBuildNumber == Version.INVALID)
        {
            return requiredTasks;
        }

        for (UpgradeTask task : upgradeTasks)
        {
            if (fromBuildNumber < task.getBuildNumber())
            {
                if (toBuildNumber == Version.INVALID || task.getBuildNumber() <= toBuildNumber)
                {
                    requiredTasks.add(task);
                }
            }
        }

        Collections.sort(requiredTasks, new UpgradeTaskComparator());
        return requiredTasks;
    }

    /**
     * This list contains all of the upgrade tasks that need to be executed.
     *
     * NOTE: This includes upgrade tasks that are run for every upgrade. DO NOT use this list to
     * determine whether or not an upgrade is required. Instead, use #determineRequiredUpgradeTasks
     *
     * @param fromBuildNumber the build number we are upgrading from
     * @param toBuildNumber the build number we are upgrading to
     *
     * @return an ordered list of all the upgrade tasks that will be run during the upgrade.
     *
     * @see #determineRequiredUpgradeTasks(int, int)
     */
    protected List<UpgradeTask> compileUpgradeTasks(int fromBuildNumber, int toBuildNumber)
    {
        List<UpgradeTask> tasks = determineRequiredUpgradeTasks(fromBuildNumber, toBuildNumber);

        tasks.addAll(systemTasks);
        Collections.sort(tasks, new UpgradeTaskComparator());
        
        return tasks;
    }

    /**
     * Check if the specified data directory requires an upgrade to be used with the
     * current installation.
     *
     * @param data directory being upgraded.
     *
     * @return true if an upgrade is required, false otherwise.
     */
    public boolean isUpgradeRequired(Data data)
    {
        checkData(data);

        Version from = data.getVersion();
        Version to = Version.getVersion();

        return isUpgradeRequired(from.getBuildNumberAsInt(), to.getBuildNumberAsInt());
    }

    /**
     * Prepare to upgrade the specified data directory. Prepare must be called before
     * the upgrade can be executed and before any specific details about the upgrade
     * are available.
     *
     * @param data directory being upgraded
     */
    public void prepareUpgrade(Data data)
    {
        checkData(data);

        // ensure that
        // a) upgrade is not in progress.

        Version from = data.getVersion();
        Version to = Version.getVersion();

        List<UpgradeTask> tasks = compileUpgradeTasks(from.getBuildNumberAsInt(), to.getBuildNumberAsInt());
        for(UpgradeTask task: tasks)
        {
            if(ConfigurationAware.class.isAssignableFrom(task.getClass()))
            {
                ((ConfigurationAware)task).setConfigurationManager(configurationManager);
            }
        }

        currentContext = new DefaultUpgradeContext(from, to);
        currentContext.setTasks(tasks);

        monitor = new UpgradeProgressMonitor();

        upgradeTarget = data;
    }

    /**
     * Retrieve the list of upgrade tasks that will be executed when the next
     * upgrade is triggered.
     *
     * @return a list of upgrade tasks.
     *
     * @throws IllegalArgumentException if prepareUpgrade has not been called.
     */
    public List<UpgradeTask> previewUpgrade()
    {
        if (currentContext == null)
        {
            throw new IllegalArgumentException("No upgrade has been prepared.");
        }
        return currentContext.getTasks();
    }

    /**
     * Start executing the upgrade.
     *
     */
    public void executeUpgrade()
    {
        currentContext.setData(upgradeTarget);

        List<UpgradeTask> tasksToExecute = currentContext.getTasks();
        UpgradeContext context = currentContext;

        monitor.setTasks(tasksToExecute);
        monitor.start();

        boolean abort = false;
        for (UpgradeTask task : tasksToExecute)
        {
            try
            {
                if (!abort)
                {
                    monitor.start(task);
                    try
                    {
                        task.execute(context);
                    }
                    catch (UpgradeException e)
                    {
                        throw e;
                    }
                    catch (Throwable t)
                    {
                        throw new UpgradeException(t);
                    }

                    if (task.hasFailed())
                    {
                        // use an exception to break out to the task failure handling.
                        throw new UpgradeException(String.format("Task %s has been marked as failed.", task.getName()));
                    }
                    monitor.complete(task);
                    
                    // record task completion, to ensure that it is not run a second time. Any task with a build
                    // number less than zero are run during each upgrade and do not impact the target build number.
                    if (task.getBuildNumber() > 0)
                    {
                        upgradeTarget.setBuildNumber(task.getBuildNumber());
                    }
                }
                else
                {
                    monitor.aborted(task);
                }
            }
            catch (UpgradeException e)
            {
                monitor.failed(task);
                if (task.haltOnFailure())
                {
                    abort = true;
                }
                LOG.severe(e);
            }
        }

        // if the upgrade was successful, upgrade the version details for the data directory.
        if (monitor.isSuccessful())
        {
            upgradeTarget.updateVersion(context.getTo());
        }

        monitor.setPercentageComplete(100);
        monitor.stop();
    }

    public UpgradeProgressMonitor getUpgradeMonitor()
    {
        return monitor;
    }

    /**
     * Check that the specified data instance if a valid instance to be working
     * with.
     *
     * @param data instance
     *
     * @throws IllegalArgumentException if the data instance is not valid.
     */
    private void checkData(Data data)
    {
        if (!data.isInitialised())
        {
            throw new IllegalArgumentException();
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
