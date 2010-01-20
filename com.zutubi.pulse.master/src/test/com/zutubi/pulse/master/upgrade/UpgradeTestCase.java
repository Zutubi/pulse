package com.zutubi.pulse.master.upgrade;

import com.zutubi.pulse.master.util.monitor.JobListener;
import com.zutubi.pulse.master.util.monitor.Task;
import com.zutubi.pulse.master.util.monitor.TaskFeedback;
import com.zutubi.util.junit.ZutubiTestCase;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class UpgradeTestCase extends ZutubiTestCase
{
    protected static class UpgradeableComponentAdapter implements UpgradeableComponent, JobListener<UpgradeTaskAdapter>
    {
        private boolean upgradeRequired = false;
        private List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();

        protected List<Task> failedTasks = new LinkedList<Task>();
        protected List<Task> abortedTasks = new LinkedList<Task>();
        protected List<Task> completedTasks = new LinkedList<Task>();

        private boolean wasStarted;
        private boolean wasCompleted;
        private boolean wasAborted;

        public UpgradeableComponentAdapter()
        {

        }

        public UpgradeableComponentAdapter(List<UpgradeTaskAdapter> tasks)
        {
            this.upgradeRequired = tasks.size() > 0;
            this.tasks = tasks;
        }

        public boolean isUpgradeRequired()
        {
            return upgradeRequired;
        }

        public List<UpgradeTask> getUpgradeTasks()
        {
            return new LinkedList<UpgradeTask>(tasks);
        }

        public void upgradeStarted()
        {
            this.wasStarted = true;
        }

        public void upgradeCompleted()
        {
            this.wasCompleted = true;
        }

        public void upgradeAborted()
        {
            this.wasAborted = true;
        }

        public boolean wasStarted()
        {
            return wasStarted;
        }

        public boolean wasCompleted()
        {
            return wasCompleted;
        }

        public boolean wasAborted()
        {
            return wasAborted;
        }

        public void taskCompleted(UpgradeTaskAdapter task, TaskFeedback<UpgradeTaskAdapter> feedback)
        {
            completedTasks.add(task);
            assertTrue(tasks.contains(task));
        }

        public void taskFailed(UpgradeTaskAdapter task, TaskFeedback<UpgradeTaskAdapter> feedback)
        {
            failedTasks.add(task);
            assertTrue(tasks.contains(task));
        }

        public void taskAborted(UpgradeTaskAdapter task, TaskFeedback feedback)
        {
            abortedTasks.add(task);
            assertTrue(tasks.contains(task));
        }

        public void taskStarted(UpgradeTaskAdapter task, TaskFeedback<UpgradeTaskAdapter> feedback)
        {
            assertTrue(tasks.contains(task));
        }
    }

    protected static class UpgradeTaskAdapter implements UpgradeTask
    {
        private boolean executed = false;
        private boolean failed = false;
        private boolean haltOnFailure = false;
        private boolean fail = false;

        public UpgradeTaskAdapter()
        {

        }

        public UpgradeTaskAdapter(boolean haltOnFailure, boolean fail)
        {
            this.haltOnFailure = haltOnFailure;
            this.fail = fail;
        }

        public void execute()
        {
            executed = true;
            failed = fail;
        }

        public boolean isExecuted()
        {
            return executed;
        }

        public String getName()
        {
            return "Upgrade Task Adapter";
        }

        public String getDescription()
        {
            return "Test Only implementation";
        }

        public boolean haltOnFailure()
        {
            return haltOnFailure;
        }

        public boolean hasFailed()
        {
            return failed;
        }

        public List<String> getErrors()
        {
            return new LinkedList<String>();
        }

        void setHaltOnFailure(boolean b)
        {
            this.haltOnFailure = b;
        }

        void setFail(boolean b)
        {
            this.fail = b;
        }
    }

    protected static class UpgradeableComponentSourceAdapter implements UpgradeableComponentSource
    {
        private List<UpgradeableComponent> components = new LinkedList<UpgradeableComponent>();

        public UpgradeableComponentSourceAdapter()
        {
        }

        public UpgradeableComponentSourceAdapter(List<UpgradeableComponent> components)
        {
            this.components = components;
        }

        public UpgradeableComponentSourceAdapter(UpgradeableComponent component)
        {
            this.components = Arrays.asList(component);
        }

        public boolean isUpgradeRequired()
        {
            return components.size() > 0;
        }

        public List<UpgradeableComponent> getUpgradeableComponents()
        {
            return components;
        }
    }
}
