package com.zutubi.pulse.upgrade;

import com.zutubi.pulse.Version;
import com.zutubi.pulse.bootstrap.ComponentContext;
import com.zutubi.pulse.bootstrap.Data;
import com.zutubi.pulse.util.logging.Logger;

import javax.sql.DataSource;
import java.io.IOException;
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

    /**
     * The upgrade context for the current upgrade. This will be null if no upgrade
     * is in progress / prepared.
     */
    private DefaultUpgradeContext currentContext;

    private UpgradeProgressMonitor monitor;

    private Data upgradeTarget;

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
     * @param tasks
     */
    public void setTasks(List<UpgradeTask> tasks)
    {
        this.upgradeTasks = tasks;
    }

    /**
     * Determine if an upgrade is required between the specified versions.
     *
     * @param fromVersion
     * @param toVersion
     *
     * @return true if an upgrade is required, false otherwise.
     */
    public boolean isUpgradeRequired(Version fromVersion, Version toVersion)
    {
        if ( fromVersion.compareTo(toVersion) < 0 )
        {
            List<UpgradeTask> requiredTasks = determineRequiredUpgradeTasks(fromVersion, toVersion);
            if (requiredTasks.size() > 0)
            {
                return true;
            }
        }
        return false;
    }

    public List<UpgradeTask> previewUpgrade(Version fromVersion, Version toVersion)
    {
        return determineRequiredUpgradeTasks(fromVersion, toVersion);
    }

    /**
     * Determine which upgrade tasks need to be executed during an upgrade between the
     * indicated versions.
     *
     * @param fromVersion specifies the lower version and is not included in the determination.
     * @param toVersion specified the upper version and is included in the determination.
     *
     * @return a list of upgrade tasks that are required.
     */
    protected List<UpgradeTask> determineRequiredUpgradeTasks(Version fromVersion, Version toVersion)
    {
        List<UpgradeTask> requiredTasks = new LinkedList<UpgradeTask>();

        // if either build versions are invalid, then we do not attempt an upgrade.
        if (!checkVersions(fromVersion, toVersion))
        {
            return requiredTasks;
        }

        int from = fromVersion.getBuildNumberAsInt();
        int to = toVersion.getBuildNumberAsInt();

        for (UpgradeTask task : upgradeTasks)
        {
            if (from < task.getBuildNumber() && task.getBuildNumber() <= to)
            {
                requiredTasks.add(task);
            }
        }
        Collections.sort(requiredTasks, new UpgradeTaskComparator());
        return requiredTasks;
    }

    private boolean checkVersions(Version from, Version to)
    {
        if (from.getBuildNumberAsInt() == Version.INVALID)
        {
            LOG.warning("invalid from version build number detected: '"+from.getBuildNumber()+"'");
            return false;
        }
        if (to.getBuildNumberAsInt() == Version.INVALID)
        {
            LOG.warning("invalid 'to' version build number detected: '"+to.getBuildNumber()+"'");
            return false;
        }
        return true;
    }

    /**
     * Check if the specified data directory requires an upgrade to be used with the
     * current installation.
     *
     * @param data
     *
     * @return true if an upgrade is required, false otherwise.
     */
    public boolean isUpgradeRequired(Data data)
    {
        checkData(data);

        Version from = data.getVersion();
        Version to = Version.getVersion();

        return isUpgradeRequired(from, to);
    }

    /**
     * Prepare to upgrade the specified data directory. Prepare must be called before
     * the upgrade can be executed and before any specific details about the upgrade
     * are available.
     *
     * @param data
     */
    public void prepareUpgrade(Data data)
    {
        checkData(data);

        // load the datasource as some tasks require it
        ComponentContext.addClassPathContextDefinitions("com/zutubi/pulse/bootstrap/context/databaseContext.xml");
        DataSource dataSource = (DataSource) ComponentContext.getBean("dataSource");

        // ensure that
        // a) upgrade is not in progress.

        Version from = data.getVersion();
        Version to = Version.getVersion();

        List<UpgradeTask> tasks = determineRequiredUpgradeTasks(from, to);
        for(UpgradeTask task: tasks)
        {
            if(DataSourceAware.class.isAssignableFrom(task.getClass()))
            {
                ((DataSourceAware)task).setDataSource(dataSource);
            }
        }

        // copy the tasks...

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
     */
    public List<UpgradeTask> previewUpgrade()
    {
        if (currentContext == null)
        {
            throw new IllegalArgumentException("no upgrade has been prepared.");
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
                    task.execute(context);
                    if (task.hasFailed())
                    {
                        // use an exception to break out to the task failure handling.
                        throw new UpgradeException();
                    }
                    monitor.complete(task);
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
            }
        }

        monitor.setPercentageComplete(99);

        // TODO: improve UI feedback for abort situation so that the user knows what is going on.
        // if upgrade aborted due to a failure, then the user will need to go back to there backup
        // of the data directory and upgrade again once the problem has been sorted out.

        // commit the upgrade by updating the data version details.
        try
        {
            upgradeTarget.updateVersion(context.getTo());
        }
        catch (IOException e)
        {
            // record this error for the user...
            LOG.severe(e);
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
     * @param data
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
}
