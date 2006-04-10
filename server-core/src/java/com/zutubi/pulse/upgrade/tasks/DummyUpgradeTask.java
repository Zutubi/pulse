package com.cinnamonbob.upgrade.tasks;

import com.cinnamonbob.upgrade.UpgradeContext;
import com.cinnamonbob.upgrade.UpgradeException;
import com.cinnamonbob.upgrade.UpgradeTask;

import java.util.List;

/**
 * <class-comment/>
 */
public class DummyUpgradeTask implements UpgradeTask
{
    private String description;
    private String name;
    private int buildNumber;

    private long sleep = 5000;
    private boolean haltOnError = false;

    private boolean fail = false;

    public DummyUpgradeTask()
    {
    }

    public int getBuildNumber()
    {
        return this.buildNumber;
    }

    public void setBuildNumber(int buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public void execute(UpgradeContext context) throws UpgradeException
    {
        // lets wait a bit.
        try
        {
            Thread.sleep(sleep);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        if (fail)
        {
            throw new UpgradeException();
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public List<String> getErrors()
    {
        return null;
    }

    public boolean haltOnFailure()
    {
        return haltOnError;
    }

    public void setHaltOnError(boolean haltOnError)
    {
        this.haltOnError = haltOnError;
    }

    public void setSleep(long sleep)
    {
        this.sleep = sleep;
    }

    public void setFail(boolean fail)
    {
        this.fail = fail;
    }
}
