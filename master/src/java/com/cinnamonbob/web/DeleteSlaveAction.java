package com.zutubi.pulse.web;

import com.zutubi.pulse.model.SlaveManager;

/**
 * <class-comment/>
 */
public class DeleteSlaveAction extends ActionSupport
{
    private SlaveManager slaveManager;

    private long id;

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return id;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        if (slaveManager.getSlave(id) == null)
        {
            addFieldError("id", "A slave with the id '" + id + "' does not exist.");
        }
    }

    public String execute()
    {
        slaveManager.delete(id);
        return SUCCESS;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }
}
