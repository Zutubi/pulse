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

    protected static class UpgradeableComponentAdapter implements UpgradeableComponent
    {
        private boolean upgradeRequired = false;
        private List<UpgradeTaskAdapter> tasks = new LinkedList<UpgradeTaskAdapter>();

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

        public void prepareUpgrade()
        {

        }

        public void completeUpgrade()
        {

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
