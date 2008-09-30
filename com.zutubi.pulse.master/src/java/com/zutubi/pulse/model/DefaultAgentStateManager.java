package com.zutubi.pulse.model;

import com.zutubi.pulse.agent.AgentPingService;
import com.zutubi.pulse.model.persistence.AgentStateDao;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.SimpleTrigger;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.tasks.PingSlaves;
import com.zutubi.util.Constants;
import com.zutubi.util.logging.Logger;

import java.util.List;

/**
 */
public class DefaultAgentStateManager implements AgentStateManager
{
    private static final Logger LOG = Logger.getLogger(DefaultAgentStateManager.class);

    private static final String PING_NAME = "ping";
    private static final String PING_GROUP = "services";

    private AgentStateDao agentStateDao;
    private Scheduler scheduler;
    private ProjectManager projectManager;

    public void init()
    {
        // register a schedule for pinging the slaves.
        // check if the trigger exists. if not, create and schedule.
        Trigger trigger = scheduler.getTrigger(PING_NAME, PING_GROUP);
        if (trigger != null)
        {
            return;
        }

        // initialise the trigger.
        trigger = new SimpleTrigger(PING_NAME, PING_GROUP, AgentPingService.getAgentPingInterval() * Constants.SECOND);
        trigger.setTaskClass(PingSlaves.class);

        try
        {
            scheduler.schedule(trigger);
        }
        catch (SchedulingException e)
        {
            LOG.severe(e);
        }
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
        AgentState agentState = agentStateDao.findById(id);
        if (agentState != null)
        {
            projectManager.removeReferencesToAgent(id);
            agentStateDao.delete(agentState);
        }
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

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}