package com.cinnamonbob.upgrade.tasks;

import com.cinnamonbob.upgrade.UpgradeContext;
import com.cinnamonbob.upgrade.UpgradeException;
import com.cinnamonbob.upgrade.UpgradeTask;

import java.util.Collections;
import java.util.List;

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

    public String getName()
    {
        return "Mock upgrade";
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
