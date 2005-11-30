package com.cinnamonbob.web;

import com.cinnamonbob.model.Slave;
import com.cinnamonbob.model.persistence.SlaveDao;
import com.cinnamonbob.web.user.UserActionSupport;

/**
 * 
 *
 */
public class CreateSlaveAction extends UserActionSupport
{
    private Slave slave = new Slave();
    private SlaveDao slaveDao;

    public Slave getSlave()
    {
        return slave;
    }

    public void validate()
    {
        if (hasErrors())
        {
            // do not attempt to validate unless all other validation rules have 
            // completed successfully.
            return;
        }

/*
        if (slaveDao.getUser(slave.getName()) != null)
        {
            // login name already in use.
            addFieldError("user.login", "Login name " + user.getLogin() + " is already being used.");
        }
*/
    }

    public String execute()
    {
        // store user.
        slaveDao.save(slave);

        return SUCCESS;
    }

    public String doDefault()
    {
        // setup any default data.
        return SUCCESS;
    }

    public void setSlaveDao(SlaveDao slaveDao)
    {
        this.slaveDao = slaveDao;
    }
}
