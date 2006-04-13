/********************************************************************************
  @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web;

import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;

/**
 * <class-comment/>
 */
public class EditSlaveAction extends ActionSupport
{
    private SlaveManager slaveManager;

    private long id;
    private Slave slave = new Slave();

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public Slave getSlave()
    {
        return slave;
    }

    public String doDefault()
    {
        slave = slaveManager.getSlave(id);
        return SUCCESS;
    }

    public String execute()
    {
        Slave persistentSlave = slaveManager.getSlave(id);
        persistentSlave.setHost(slave.getHost());
        persistentSlave.setName(slave.getName());
        persistentSlave.setPort(slave.getPort());
        slaveManager.save(persistentSlave);

        return SUCCESS;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }
}
