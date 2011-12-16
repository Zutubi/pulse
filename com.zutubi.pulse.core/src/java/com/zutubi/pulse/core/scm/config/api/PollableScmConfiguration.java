package com.zutubi.pulse.core.scm.config.api;

import com.zutubi.tove.annotations.ControllingCheckbox;
import com.zutubi.tove.annotations.StringList;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wizard;
import com.zutubi.validation.annotations.Numeric;

import java.util.LinkedList;
import java.util.List;

/**
 * A supporting base class for configuration of SCMs that support polling.
 */
@SymbolicName("zutubi.pollableScmConfig")
public abstract class PollableScmConfiguration extends ScmConfiguration implements Pollable
{
    @Wizard.Ignore
    private boolean monitor = true;

    @ControllingCheckbox(checkedFields = {"pollingInterval"})
    @Wizard.Ignore
    private boolean customPollingInterval = false;
    /**
     * Number of minutes between polls of this SCM.
     */
    @Numeric(min = 1)
    @Wizard.Ignore
    private int pollingInterval = 1;

    @ControllingCheckbox(checkedFields = {"quietPeriod"})
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
    private List<String> includedPaths = new LinkedList<String>();

    @StringList
    @Wizard.Ignore
    private List<String> excludedPaths = new LinkedList<String>();

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

    public List<String> getIncludedPaths()
    {
        return includedPaths;
    }

    public void setIncludedPaths(List<String> includedPaths)
    {
        this.includedPaths = includedPaths;
    }

    public List<String> getExcludedPaths()
    {
        return excludedPaths;
    }

    public void setExcludedPaths(List<String> excludedPaths)
    {
        this.excludedPaths = excludedPaths;
    }
}
