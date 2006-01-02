package com.cinnamonbob.web;

import com.cinnamonbob.model.persistence.SlaveDao;
import com.cinnamonbob.model.Slave;

/**
 * <class-comment/>
 */
public class EditSlaveAction extends ActionSupport
{
    private SlaveDao dao;

    private long id;
    private Slave slave = new Slave();

    public void setSlaveDao(SlaveDao dao)
    {
        this.dao = dao;
    }

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
        slave = dao.findById(id);
        return SUCCESS;
    }

    public String execute()
    {
        Slave persistentSlave = dao.findById(id);
        persistentSlave.setHost(slave.getHost());
        persistentSlave.setName(slave.getName());
        persistentSlave.setPort(slave.getPort());
        dao.save(persistentSlave);
        
        return SUCCESS;
    }
}
