package com.zutubi.pulse.agent;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.model.NamedEntity;
import com.zutubi.pulse.model.Slave;

import java.util.List;

/**
 * An abstraction over an agent, be it the local (master) agent or a slave.
 */
public interface Agent extends NamedEntity
{
    long getId();
    Status getStatus();
    void updateStatus(Status status);
    String getLocation();

    boolean isSlave();
    Slave.EnableState getEnableState();
    boolean isEnabled();
    boolean isDisabling();
    BuildService getBuildService();
    long getRecipeId();

    SystemInfo getSystemInfo();

    List<CustomLogRecord> getRecentMessages();

    boolean isOnline();

}
