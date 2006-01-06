package com.cinnamonbob.web;

import com.cinnamonbob.model.Slave;
import com.cinnamonbob.model.SlaveManager;

/**
 * 
 *
 */
public class CreateSlaveAction extends ActionSupport
{
    private Slave slave = new Slave();
    private SlaveManager slaveManager;

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

        if (slaveManager.getSlave(slave.getName()) != null)
        {
            // slave name already in use.
            addFieldError("slave.name", "A slave with name '" + slave.getName() + "' already exists.");
        }
    }

    public String execute()
    {
        slaveManager.save(slave);
        return SUCCESS;
    }

    public String doDefault()
    {
        // setup any default data.
        return SUCCESS;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }
}
