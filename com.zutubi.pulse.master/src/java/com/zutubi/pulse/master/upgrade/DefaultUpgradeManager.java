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

    private JobRunner runner;

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

        runner = new JobRunner();

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

        Monitor monitor = runner.getMonitor();

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
        runner.getMonitor().add(new UpgradeTaskLogger(runner.getMonitor()));
        runner.getMonitor().add(job);
        runner.run(job);

        for (UpgradeTaskGroup group : groups)
        {
            boolean abort = false;

            for (Task task : group.getTasks())
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

    private class DelegateJobListener implements JobListener
    {
        private JobListener delegate;

        public DelegateJobListener()
        {
        }

        public DelegateJobListener(JobListener delegate)
        {
            this.delegate = delegate;
        }

        public void taskCompleted(Task task)
        {
            if (delegate != null)
            {
                delegate.taskCompleted(task);
            }
        }

        public void taskFailed(Task task)
        {
            if (delegate != null)
            {
                delegate.taskFailed(task);
            }
        }

        public void taskAborted(Task task)
        {
            if (delegate != null)
            {
                delegate.taskAborted(task);
            }
        }

        public void taskStarted(Task task)
        {
            if (delegate != null)
            {
                delegate.taskStarted(task);
            }
        }
    }

    private class UpgradeTaskGroupJobAdapter implements Job<UpgradeTask>, Iterator<UpgradeTask>, JobListener<UpgradeTask>
    {
        private Iterator<UpgradeTaskGroup> groups;

        private Iterator<UpgradeTask> tasks;

        private JobListener listener = new DelegateJobListener();

        private Map<UpgradeTask, JobListener<UpgradeTask>> taskListeners = new HashMap<UpgradeTask, JobListener<UpgradeTask>>();

        public UpgradeTaskGroupJobAdapter(List<UpgradeTaskGroup> groups)
        {
            this.groups = groups.iterator();

            UpgradeTaskGroup currentGroup = this.groups.next();
            UpgradeableComponent source = currentGroup.getSource();
            if (source instanceof JobListener)
            {
                listener = new DelegateJobListener((JobListener) source);
            }

            this.tasks = currentGroup.getTasks().iterator();
        }

        public void taskStarted(UpgradeTask task)
        {
            if (taskListeners.containsKey(task))
            {
                taskListeners.get(task).taskStarted(task);
            }
        }

        public void taskCompleted(UpgradeTask task)
        {
            if (taskListeners.containsKey(task))
            {
                taskListeners.get(task).taskCompleted(task);
            }
        }

        public void taskFailed(UpgradeTask task)
        {
            if (taskListeners.containsKey(task))
            {
                taskListeners.get(task).taskFailed(task);
            }
        }

        public void taskAborted(UpgradeTask task)
        {
            if (taskListeners.containsKey(task))
            {
                taskListeners.get(task).taskAborted(task);
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
