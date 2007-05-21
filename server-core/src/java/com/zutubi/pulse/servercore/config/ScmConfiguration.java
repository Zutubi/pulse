package com.zutubi.pulse.servercore.config;

import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Transient;
import com.zutubi.config.annotations.ControllingCheckbox;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.servercore.scm.ScmClient;
import com.zutubi.pulse.core.config.AbstractConfiguration;
import com.zutubi.validation.annotations.Numeric;

import java.util.List;

/**
 *
 */
@SymbolicName("internal.scmConfig")
public abstract class ScmConfiguration extends AbstractConfiguration
{
    private boolean monitor;
    
    @ControllingCheckbox(dependentFields = {"pollingInterval"})
    private boolean customPollingInterval;
    /**
     * Number of minutes between polls of this SCM.
     */
    @Numeric(min = 1)
    private int pollingInterval = 1;
    
    @ControllingCheckbox(dependentFields = {"quietPeriod"})
    private boolean quietPeriodEnabled;
    /**
     * Quiet period, i.e. idle time to wait for between checkins before
     * raising a change event, measured in minutes.
     */
    @Numeric(min = 1)
    private int quietPeriod = 1;
    private List<String> filterPaths;

    public boolean getMonitor()
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

    @Transient
    public abstract String getType();
    public abstract ScmClient createClient() throws ScmException;
}
