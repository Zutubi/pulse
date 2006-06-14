package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.pulse.core.model.Resource;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;

/**
 * Represents a slave server that builds may be farmed out to.
 */
public class Slave extends Entity
{
    private String name;
    private String host;
    private int port = 8090;

    public Slave()
    {

    }

    public Slave(String name, String host)
    {
        this.name = name;
        this.host = host;
    }

    public Slave(String name, String host, int port)
    {
        super();
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

}
