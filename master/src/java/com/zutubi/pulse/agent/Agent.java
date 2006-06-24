package com.zutubi.pulse.agent;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.model.NamedEntity;

import java.util.List;

/**
 * An abstraction over an agent, be it the local (master) agent or a slave.
 */
public interface Agent extends NamedEntity
{
    long getId();
    Status getStatus();
    String getLocation();

    boolean isSlave();
    BuildService getBuildService();

    SystemInfo getSystemInfo();

    List<CustomLogRecord> getRecentMessages();

    boolean isOnline();
}
