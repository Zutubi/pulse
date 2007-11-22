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

    private List<UpgradeableComponentSource> upgradeableSources = new LinkedList<UpgradeableComponentSource>();

    private List<UpgradeableComponent> upgradeableComponents = new LinkedList<UpgradeableComponent>();

    private List<UpgradeTaskGroup> groups = new LinkedList<UpgradeTaskGroup>();

    private UpgradeProgressMonitor monitor;

    public boolean isUpgradeRequired()
    {
        for (UpgradeableComponentSource source : upgradeableSources)
        {
            if (source.isUpgradeRequired())
            {
                return true;
            }
        }

        for (UpgradeableComponent component : upgradeableComponents)
        {
            if (component.isUpgradeRequired())
            {
                return true;
            }
        }
        return false;
    }

    public void add(UpgradeableComponent component)
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

    public void add(UpgradeableComponentSource componentSource) 
    {
        upgradeableSources.add(componentSource);
    }

    public void setUpgradeableComponentSources(List<UpgradeableComponentSource> componentSources)
    {
        this.upgradeableSources = componentSources;
    }

    public void setUpgradeableComponentSources(UpgradeableComponentSource... componentSources)
    {
        this.upgradeableSources = Arrays.asList(componentSources);
    }

    public void prepareUpgrade()
    {
        groups = new LinkedList<UpgradeTaskGroup>();

        List<UpgradeableComponent> components = new LinkedList<UpgradeableComponent>();
        components.addAll(upgradeableComponents);
        for (UpgradeableComponentSource source : upgradeableSources)
        {
            if (source.isUpgradeRequired())
            {
                components.addAll(source.getUpgradeableComponents());
            }
        }

        for (UpgradeableComponent component : components)
        {
            if (component.isUpgradeRequired())
            {
                // task group takes details from the upgradeable component.
                UpgradeTaskGroup taskGroup = new UpgradeTaskGroup();
                taskGroup.setSource(component);
                taskGroup.setTasks(component.getUpgradeTasks());
                groups.add(taskGroup);
            }
        }

        monitor = new UpgradeProgressMonitor();
        monitor.setTaskGroups(groups);
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

        for (UpgradeTaskGroup group : groups)
        {
            monitor.started(group);

            UpgradeableComponent source = group.getSource();

            UpgradeListener listener = new DelegateUpgradeListener();
            if (source instanceof UpgradeListener)
            {
                listener = new DelegateUpgradeListener((UpgradeListener) group.getSource());
            }

            source.upgradeStarted();

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
                            StringBuffer errors = new StringBuffer();
                            String sep = "\n";
                            for (String error : task.getErrors())
                            {
                                errors.append(sep);
                                errors.append(error);
                            }

                            throw new UpgradeException("UpgradeTask '" + task.getName() + "' is marked as failed. " +
                                    "The following errors were recorded:" + errors.toString());
                        }

                        monitor.completed(task);
                        listener.taskCompleted(task);
                    }
                    else
                    {
                        monitor.aborted(task);
                        listener.taskAborted(task);
                    }
                }
                catch (UpgradeException e)
                {
                    monitor.failed(task);
                    listener.taskFailed(task);
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
                source.upgradeAborted();
            }
            else
            {
                monitor.completed(group);
                source.upgradeCompleted();
            }
        }

        monitor.finish();
    }

    public UpgradeProgressMonitor getUpgradeMonitor()
    {
        return monitor;
    }

    private class DelegateUpgradeListener implements UpgradeListener
    {
        private UpgradeListener delegate;

        public DelegateUpgradeListener()
        {
        }

        public DelegateUpgradeListener(UpgradeListener delegate)
        {
            this.delegate = delegate;
        }

        public void taskCompleted(UpgradeTask task)
        {
            if (delegate != null)
            {
                delegate.taskCompleted(task);
            }
        }

        public void taskFailed(UpgradeTask task)
        {
            if (delegate != null)
            {
                delegate.taskFailed(task);
            }
        }

        public void taskAborted(UpgradeTask task)
        {
            if (delegate != null)
            {
                delegate.taskAborted(task);
            }
        }
    }
}
