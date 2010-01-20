package com.zutubi.pulse.master.upgrade;

import com.zutubi.pulse.master.util.monitor.*;
import com.zutubi.util.logging.Logger;

import java.util.*;

/**
 * The default implementation of the upgrade manager interface for the Pulse server.
 */
public class DefaultUpgradeManager implements UpgradeManager
{
    private static final Logger LOG = Logger.getLogger(UpgradeManager.class);

    private List<UpgradeableComponentSource> upgradeableSources = new LinkedList<UpgradeableComponentSource>();

    private List<UpgradeableComponent> upgradeableComponents = new LinkedList<UpgradeableComponent>();

    private List<UpgradeTaskGroup> groups = new LinkedList<UpgradeTaskGroup>();

    private JobRunner<UpgradeTask> runner;

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

    /**
     * Register an upgradeable component with the upgrade manager
     *
     * @param component the component being registered.
     */
    public void add(UpgradeableComponent component)
    {
        upgradeableComponents.add(component);
    }

    /**
     * Set the list of upgradeable components known by the upgrade manager.  This replaces
     * any previously registered upgradeable components.
     *
     * @param components    the new definitive list of upgradeable components.
     */
    public void setUpgradeableComponents(List<UpgradeableComponent> components)
    {
        this.upgradeableComponents = components;
    }

    /**
     * Set an array of upgradeable components known by the upgrade manager.  This replaces
     * any previously registered upgradeable components.
     *
     * @param components    the new definitive array of upgradeable components.
     */
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

    public List<UpgradeTaskGroup> prepareUpgrade()
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

        runner = new JobRunner<UpgradeTask>();

        return Collections.unmodifiableList(groups);
    }

    public List<UpgradeTaskGroup> previewUpgrade()
    {
        assertUpgradePrepared();

        return Collections.unmodifiableList(groups);
    }

    public void executeUpgrade()
    {
        assertUpgradePrepared();

        Monitor<UpgradeTask> monitor = runner.getMonitor();

        // CIB-1029: Refresh during upgrade causes tasks to be re-run
        // The upgrade manager handles a one-shot process.  At no stage should executeUpgrade be allowed
        // to proceed a second time.  Enforce this just in case the client gets it wrong.
        if (monitor.isStarted())
        {
            LOG.warning("Attempted to execute an executing upgrade.  Request has been ignored.");
            return;
        }

        for (UpgradeTaskGroup group : groups)
        {
            group.getSource().upgradeStarted();
        }

        UpgradeTaskGroupJobAdapter job = new UpgradeTaskGroupJobAdapter(groups);
        runner.getMonitor().add(new UpgradeTaskLogger());
        runner.getMonitor().add(job);
        runner.run(job);

        for (UpgradeTaskGroup group : groups)
        {
            boolean abort = false;

            for (UpgradeTask task : group.getTasks())
            {
                TaskFeedback progress = monitor.getProgress(task);
                if (progress.isFailed() && task.haltOnFailure())
                {
                    abort = true;
                    break;
                }
            }

            UpgradeableComponent source = group.getSource();
            if (abort)
            {
                source.upgradeAborted();
            }
            else
            {
                source.upgradeCompleted();
            }
        }
    }

    private void assertUpgradePrepared()
    {
        if (groups == null)
        {
            throw new IllegalStateException("You can not execute the upgrade before it has been prepared.");
        }
    }

    public Monitor getMonitor()
    {
        return runner == null ? null : runner.getMonitor();
    }

    private class DelegateJobListener implements JobListener<UpgradeTask>
    {
        private JobListener<UpgradeTask> delegate;

        public DelegateJobListener()
        {
        }

        public DelegateJobListener(JobListener<UpgradeTask> delegate)
        {
            this.delegate = delegate;
        }

        public void taskCompleted(UpgradeTask task, TaskFeedback<UpgradeTask> feedback)
        {
            if (delegate != null)
            {
                delegate.taskCompleted(task, null);
            }
        }

        public void taskFailed(UpgradeTask task, TaskFeedback<UpgradeTask> feedback)
        {
            if (delegate != null)
            {
                delegate.taskFailed(task, null);
            }
        }

        public void taskAborted(UpgradeTask task, TaskFeedback feedback)
        {
            if (delegate != null)
            {
                delegate.taskAborted(task, null);
            }
        }

        public void taskStarted(UpgradeTask task, TaskFeedback<UpgradeTask> feedback)
        {
            if (delegate != null)
            {
                delegate.taskStarted(task, null);
            }
        }
    }

    /**
     * This is a job that wraps a list of UpgradeTaskGroup instances, and
     * ensures that upgradeable components in the groups are notified of
     * of task updates if necessary.
     */
    private class UpgradeTaskGroupJobAdapter implements Job<UpgradeTask>, Iterator<UpgradeTask>, JobListener<UpgradeTask>
    {
        private Iterator<UpgradeTaskGroup> groups;

        private Iterator<UpgradeTask> tasks;

        private JobListener<UpgradeTask> listener = new DelegateJobListener();

        private Map<UpgradeTask, JobListener<UpgradeTask>> taskListeners = new HashMap<UpgradeTask, JobListener<UpgradeTask>>();

        public UpgradeTaskGroupJobAdapter(List<UpgradeTaskGroup> groups)
        {
            this.groups = groups.iterator();

            UpgradeTaskGroup currentGroup = this.groups.next();
            UpgradeableComponent source = currentGroup.getSource();
            if (source instanceof JobListener)
            {
                listener = new DelegateJobListener((JobListener<UpgradeTask>) source);
            }

            this.tasks = currentGroup.getTasks().iterator();
        }

        public void taskStarted(UpgradeTask task, TaskFeedback<UpgradeTask> feedback)
        {
            if (taskListeners.containsKey(task))
            {
                taskListeners.get(task).taskStarted(task, feedback);
            }
        }

        public void taskCompleted(UpgradeTask task, TaskFeedback<UpgradeTask> feedback)
        {
            if (taskListeners.containsKey(task))
            {
                taskListeners.get(task).taskCompleted(task, feedback);
            }
        }

        public void taskFailed(UpgradeTask task, TaskFeedback<UpgradeTask> feedback)
        {
            if (taskListeners.containsKey(task))
            {
                taskListeners.get(task).taskFailed(task, feedback);
            }
        }

        public void taskAborted(UpgradeTask task, TaskFeedback feedback)
        {
            if (taskListeners.containsKey(task))
            {
                taskListeners.get(task).taskAborted(task, null);
            }
        }

        public boolean hasNext()
        {
            return tasks.hasNext() || groups.hasNext();
        }

        public UpgradeTask next()
        {
            if (tasks.hasNext())
            {
                UpgradeTask task = tasks.next();
                taskListeners.put(task, listener);
                return task;
            }

            if (groups.hasNext())
            {
                UpgradeTaskGroup currentGroup = groups.next();
                UpgradeableComponent source = currentGroup.getSource();
                if (source instanceof JobListener)
                {
                    listener = new DelegateJobListener((JobListener) source);
                }
                else
                {
                    listener = new DelegateJobListener();
                }

                tasks = currentGroup.getTasks().iterator();

                UpgradeTask task = tasks.next();
                taskListeners.put(task, listener);
                return task;
            }
            
            throw new RuntimeException();
        }

        public void remove()
        {
            throw new UnsupportedOperationException("Remove not supported.");
        }

        public Iterator<UpgradeTask> getTasks()
        {
            return this;
        }
    }
}
