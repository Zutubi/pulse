package com.zutubi.pulse.upgrade.tasks;

import com.zutubi.pulse.upgrade.UpgradeContext;
import com.zutubi.pulse.upgrade.UpgradeException;
import com.zutubi.pulse.upgrade.UpgradeTask;

import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class MockUpgradeTask implements UpgradeTask
{
    private int buildNumber;
    private boolean haltOnFailure;
    private int executionCount;

    protected List<String> errors = new LinkedList<String>();

    public MockUpgradeTask()
    {
        this(-1);
    }

    public MockUpgradeTask(int version)
    {
        this(version, false);
    }

    public MockUpgradeTask(int version, boolean haltOnFailure)
    {
        this.buildNumber = version;
        this.haltOnFailure = haltOnFailure;
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void execute(UpgradeContext context) throws UpgradeException
    {
        executionCount++;
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
        return errors;
    }

    public boolean hasFailed()
    {
        return getErrors().size() > 0;
    }

    public boolean haltOnFailure()
    {
        return haltOnFailure;
    }

    public int getExecutionCount()
    {
        return executionCount;
    }
}
