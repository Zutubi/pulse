package com.zutubi.pulse.upgrade;

import junit.framework.TestCase;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public class UpgradeTestCase extends TestCase
{
    public void testNoop()
    {
        // help idea so that it does not report a test case with not tests as bad.    
    }

    protected static class UpgradeableComponentAdapter implements UpgradeableComponent, UpgradeListener
    {
        private boolean upgradeRequired = false;
        private List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();

        protected List<UpgradeTask> failedTasks = new LinkedList<UpgradeTask>();
        protected List<UpgradeTask> abortedTasks = new LinkedList<UpgradeTask>();
        protected List<UpgradeTask> completedTasks = new LinkedList<UpgradeTask>();

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

        public void taskCompleted(UpgradeTask task)
        {
            completedTasks.add(task);
        }

        public void taskFailed(UpgradeTask task)
        {
            failedTasks.add(task);
        }

        public void taskAborted(UpgradeTask task)
        {
            abortedTasks.add(task);
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

}
