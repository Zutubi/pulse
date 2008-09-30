package com.zutubi.pulse.core.scm.config;

import com.zutubi.config.annotations.ControllingCheckbox;
import com.zutubi.config.annotations.StringList;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Wizard;
import com.zutubi.validation.annotations.Numeric;

import java.util.List;

/**
 */
@SymbolicName("zutubi.pollableScmConfig")
public abstract class PollableScmConfiguration extends ScmConfiguration implements Pollable
{
    @Wizard.Ignore
    private boolean monitor = true;

    @ControllingCheckbox(dependentFields = {"pollingInterval"})
    @Wizard.Ignore
    private boolean customPollingInterval = false;
    /**
     * Number of minutes between polls of this SCM.
     */
    @Numeric(min = 1)
    @Wizard.Ignore
    private int pollingInterval = 1;

    @ControllingCheckbox(dependentFields = {"quietPeriod"})
    @Wizard.Ignore
    private boolean quietPeriodEnabled = false;
    /**
     * Quiet period, i.e. idle time to wait for between checkins before
     * raising a change event, measured in minutes.
     */
    @Numeric(min = 1)
    @Wizard.Ignore
    private int quietPeriod = 1;

    @StringList
    @Wizard.Ignore
    private List<String> filterPaths;

    public boolean isMonitor()
    {
        return monitor;
    }

    public void setMonitor(boolean monitor)
    {
        this.monitor = monitor;
    }

    public boolean isCustomPollingInterval()
    {
        return customPollingInterval;
    }

    public void setCustomPollingInterval(boolean customPollingInterval)
    {
        this.customPollingInterval = customPollingInterval;
    }

    public int getPollingInterval()
    {
        return pollingInterval;
    }

    public void setPollingInterval(int pollingInterval)
    {
        this.pollingInterval = pollingInterval;
    }

    public boolean isQuietPeriodEnabled()
    {
        return quietPeriodEnabled;
    }

    public void setQuietPeriodEnabled(boolean quietPeriodEnabled)
    {
        this.quietPeriodEnabled = quietPeriodEnabled;
    }

    public int getQuietPeriod()
    {
        return quietPeriod;
    }

    public void setQuietPeriod(int quietPeriod)
    {
        this.quietPeriod = quietPeriod;
    }

    public List<String> getFilterPaths()
    {
        return filterPaths;
    }

    public void setFilterPaths(List<String> filterPaths)
    {
        this.filterPaths = filterPaths;
    }
}
