package com.zutubi.pulse.master.model;

import com.zutubi.pulse.master.model.persistence.AgentStateDao;

import java.util.List;

/**
 */
public class DefaultAgentStateManager implements AgentStateManager
{
    private AgentStateDao agentStateDao;
    private ProjectManager projectManager;

    public void init()
    {
    }

    public List<AgentState> getAll()
    {
        return agentStateDao.findAll();
    }

    public AgentState getAgentState(long id)
    {
        return agentStateDao.findById(id);
    }

    public void delete(long id)
    {
    }

    public void delete(AgentState agentState)
    {
        agentStateDao.delete(agentState);
    }

    public void save(AgentState agentState)
    {
        agentStateDao.save(agentState);
    }

    public void setAgentStateDao(AgentStateDao agentStateDao)
    {
        this.agentStateDao = agentStateDao;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}