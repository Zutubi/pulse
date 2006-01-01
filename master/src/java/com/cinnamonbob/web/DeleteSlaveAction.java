package com.cinnamonbob.web;

import com.cinnamonbob.model.persistence.SlaveDao;

/**
 * <class-comment/>
 */
public class DeleteSlaveAction extends ActionSupport
{
    private SlaveDao slaveDao;

    private long id;

    public void setSlaveDao(SlaveDao slaveDao)
    {
        this.slaveDao = slaveDao;
    }

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

        if (slaveDao.findById(id) == null)
        {
            addFieldError("id", "");
        }
    }

    public String execute()
    {
        return SUCCESS;
    }
}
