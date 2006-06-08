package com.zutubi.pulse.agent;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.Version;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.services.SlaveService;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 */
public class SlaveAgent implements Agent
{
    private final int masterBuildNumber = Version.getVersion().getIntBuildNumber();

    private Slave slave;
    private Status status = Status.OFFLINE;
    private long lastPingTime = 0;
    private SlaveService slaveService;
    private BuildService buildService;
    private String pingError = null;

    public SlaveAgent(Slave slave, SlaveService slaveService, BuildService buildService)
    {
        this.slave = slave;
        this.slaveService = slaveService;
        this.buildService = buildService;
    }

    public long getId()
    {
        return slave.getId();
    }

    public BuildService getBuildService()
    {
        return buildService;
    }

    public SystemInfo getSystemInfo()
    {
        return slaveService.getSystemInfo();
    }

    public List<CustomLogRecord> getRecentMessages()
    {
        return slaveService.getRecentMessages();
    }

    public String getName()
    {
        return slave.getName();
    }

    public Slave getSlave()
    {
        return slave;
    }

    public SlaveService getSlaveService()
    {
        return slaveService;
    }

    public Status getStatus()
    {
        return status;
    }

    public String getLocation()
    {
        return slave.getHost() + ":" + slave.getPort();
    }

    public boolean isSlave()
    {
        return true;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public long getLastPingTime()
    {
        return lastPingTime;
    }

    public boolean hasBeenPinged()
    {
        return lastPingTime != 0;
    }

    public String getPrettyPingTime()
    {
        if (hasBeenPinged())
        {
            return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(lastPingTime));
        }
        else
        {
            return "<never>";
        }
    }

    public long getSecondsSincePing()
    {
        return (System.currentTimeMillis() - lastPingTime) / 1000;
    }

    private void setLastPingTime(long time)
    {
        lastPingTime = time;
    }

    public void pinged(long time, int buildNumber)
    {
        lastPingTime = time;
        if(buildNumber == masterBuildNumber)
        {
            status = Status.IDLE;
        }
        else
        {
            status = Status.VERSION_MISMATCH;
        }
    }

    public void failedPing(long time, String message)
    {
        lastPingTime = time;
        pingError = message;
        status = Status.OFFLINE;
    }

    public String getPingError()
    {
        return pingError;
    }

    public boolean isOnline()
    {
        return status.ordinal() >= Status.BUILDING.ordinal();
    }
}
