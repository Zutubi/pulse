package com.zutubi.pulse.upgrade.tasks;

import java.util.List;
import java.util.LinkedList;

/**
 *
 *
 */
public abstract class AbstractUpgradeTask implements PulseUpgradeTask
{
    private int buildNumber;

    private List<String> errors;

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(int buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public boolean hasFailed()
    {
        return getErrors().size() > 0;
    }

    protected void addError(String msg)
    {
        getErrors().add(msg);
    }

    public List<String> getErrors()
    {
        if (errors == null)
        {
            synchronized(this)
            {
                if (errors == null)
                {
                    errors = new LinkedList<String>();
                }
            }
        }
        return errors;
    }
}
