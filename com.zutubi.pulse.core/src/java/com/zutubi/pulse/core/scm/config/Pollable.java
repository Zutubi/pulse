package com.zutubi.pulse.core.scm.config;

import java.util.List;

/**
 * Interface that must be implemented by SCM configurations when the SCM
 * claims capability {@link com.zutubi.pulse.core.scm.ScmCapability#POLL}.
 */
public interface Pollable
{
    boolean isMonitor();
    boolean isCustomPollingInterval();
    int getPollingInterval();
    boolean isQuietPeriodEnabled();
    int getQuietPeriod();
    List<String> getFilterPaths();
}
