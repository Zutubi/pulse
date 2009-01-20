package com.zutubi.pulse.master.upgrade.tasks;

import java.util.LinkedList;
import java.util.List;

/**
 * The AbstractUpgradeTask is an implementation of the PulseUpgradeTask
 * interface that provides default implementations for the boiler plate
 * methods.
 */
public abstract class AbstractUpgradeTask implements PulseUpgradeTask
{
    protected final UpgradeTaskMessages I18N;

    private int buildNumber;

    private List<String> errors;

    public AbstractUpgradeTask()
    {
        I18N = new UpgradeTaskMessages(getClass());
    }

    public String getName()
    {
        return I18N.getName();
    }

    public String getDescription()
    {
        return I18N.getDescription();
    }

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
