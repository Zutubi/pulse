package com.zutubi.pulse.agent;

import com.zutubi.pulse.BuildService;
import com.zutubi.pulse.SystemInfo;
import com.zutubi.pulse.logging.CustomLogRecord;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.model.Slave;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 */
public class SlaveAgent implements Agent
{
    private Slave slave;
    private Status status = Status.OFFLINE;
    private long lastPingTime = 0;
    private SlaveService slaveService;
    private BuildService buildService;

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

    public void pinged(long time, boolean succeeded)
    {
        this.lastPingTime = time;
        if (succeeded)
        {
            this.status = Status.IDLE;
        }
        else
        {
            this.status = Status.OFFLINE;
        }
    }

    public boolean isOnline()
    {
        return status != Status.OFFLINE;
    }
}
