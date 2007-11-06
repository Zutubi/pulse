package com.zutubi.pulse.upgrade;

import com.zutubi.util.logging.Logger;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;

/**
 * <class-comment/>
 */
public class DefaultUpgradeManager implements UpgradeManager
{
    private static final Logger LOG = Logger.getLogger(UpgradeManager.class);

    private List<UpgradeableComponent> upgradeableComponents = new LinkedList<UpgradeableComponent>();

    private List<UpgradeTaskGroup> groups = new LinkedList<UpgradeTaskGroup>();

    private UpgradeProgressMonitor monitor;

    public boolean isUpgradeRequired()
    {
        for (UpgradeableComponent component : upgradeableComponents)
        {
            if (component.isUpgradeRequired())
            {
                return true;
            }
        }
        return false;
    }

    public void add(UpgradeableComponent component) throws UpgradeException
    {
        upgradeableComponents.add(component);
    }

    public void setUpgradeableComponents(List<UpgradeableComponent> components)
    {
        this.upgradeableComponents = components;
    }

    public void setUpgradeableComponents(UpgradeableComponent... components)
    {
        this.upgradeableComponents = Arrays.asList(components);
    }

    public void prepareUpgrade()
    {
        groups = new LinkedList<UpgradeTaskGroup>();

        for (UpgradeableComponent component : upgradeableComponents)
        {
            if (component.isUpgradeRequired())
            {
                component.prepareUpgrade();

                // task group takes details from teh upgradeable component.
                UpgradeTaskGroup taskGroup = new UpgradeTaskGroup();
                taskGroup.setSource(component);
                taskGroup.setTasks(component.getUpgradeTasks());
                groups.add(taskGroup);
            }
        }

        monitor = new UpgradeProgressMonitor();
    }

    public List<UpgradeTaskGroup> previewUpgrade()
    {
        return Collections.unmodifiableList(groups);
    }

    public void executeUpgrade()
    {
        // CIB-1029: Refresh during upgrade causes tasks to be re-run
        // The upgrade manager handles a one-shot process.  At no stage should executeUpgrade be allowed
        // to proceed a second time.  Enforce this just in case the client gets it wrong.
        if (monitor.isStarted())
        {
            LOG.warning("Attempted to execute an executing upgrade.  Request has been ignored.");
            return;
        }

        monitor.start();
        monitor.setTaskGroups(groups);

        for (UpgradeTaskGroup group : groups)
        {
            monitor.started(group);

            UpgradeListener listener = null;
            if (group.getSource() instanceof UpgradeListener)
            {
                listener = (UpgradeListener) group.getSource();
            }

            List<UpgradeTask> tasksToExecute = group.getTasks();

            boolean abort = false;
            for (UpgradeTask task : tasksToExecute)
            {
                try
                {
                    if (!abort)
                    {
                        monitor.started(task);
                        try
                        {
                            LOG.info("Executing upgrade task: " + task.getName());
                            task.execute();
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
                            throw new UpgradeException("Task "+task.getName()+" is marked as failed.");
                        }

                        monitor.completed(task);
                        if (listener != null) // maybe we can link the listener into the monitor
                        // since the monitor knows what is happening. 
                        {
                            listener.taskComplete(task);
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

            if (abort)
            {
                monitor.aborted(group);
            }
            else
            {
                monitor.completed(group);
                group.getSource().completeUpgrade();
            }
        }

        monitor.finish();
    }

    public UpgradeProgressMonitor getUpgradeMonitor()
    {
        return monitor;
    }
}
