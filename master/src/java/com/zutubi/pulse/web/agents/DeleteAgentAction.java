package com.zutubi.pulse.web.agents;

import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.web.ActionSupport;

/**
 */
public class DeleteAgentAction extends ActionSupport
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
            addActionError("Unknown agent [" + id + "]");
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
