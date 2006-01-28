package com.cinnamonbob.upgrade.tasks;

import com.cinnamonbob.upgrade.UpgradeTask;
import com.cinnamonbob.upgrade.UpgradeException;
import com.cinnamonbob.upgrade.UpgradeContext;

import java.util.List;
import java.util.Collections;

/**
 * <class-comment/>
 */
public class MockUpgradeTask implements UpgradeTask
{
    private int buildNumber;

    public MockUpgradeTask()
    {
    }

    public MockUpgradeTask(int version)
    {
        this.buildNumber = version;
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void execute(UpgradeContext context) throws UpgradeException
    {

    }

    public String getDescription()
    {
        return "This is the mock upgrade task, useful for testing purposes only. ";
    }

    public List<String> getErrors()
    {
        return Collections.EMPTY_LIST;
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
