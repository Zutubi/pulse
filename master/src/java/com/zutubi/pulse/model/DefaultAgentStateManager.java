package com.zutubi.pulse.model;

import com.zutubi.pulse.model.persistence.AgentStateDao;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;
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

    private AgentStateDao agentStateDao;
    private BuildSpecificationDao buildSpecificationDao;
    private Scheduler scheduler;
    private ProjectManager projectManager;

    private static final String PING_NAME = "ping";
    private static final String PING_GROUP = "services";
    private static final long PING_FREQUENCY = Constants.MINUTE;

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
        trigger = new SimpleTrigger(PING_NAME, PING_GROUP, PING_FREQUENCY);
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
            // Remove all build stages that require this slave explicitly
            List<BuildSpecification> buildSpecs = buildSpecificationDao.findBySlave(agentState);
            for(BuildSpecification spec: buildSpecs)
            {
                removeStageReferences(spec.getRoot(), id);
                projectManager.save(spec);
            }

            agentStateDao.delete(agentState);
        }
    }

    private void removeStageReferences(BuildSpecificationNode node, long id)
    {
        for(BuildSpecificationNode child: node.getChildren())
        {
            BuildStage stage = child.getStage();
            if(stage != null)
            {
                BuildHostRequirements hostRequirements = stage.getHostRequirements();
                if(hostRequirements instanceof SlaveBuildHostRequirements)
                {
                    if(((SlaveBuildHostRequirements)hostRequirements).getSlave().getId() == id)
                    {
                        stage.setHostRequirements(new AnyCapableBuildHostRequirements());
                        projectManager.delete(hostRequirements);
                    }
                }
            }

            removeStageReferences(child, id);
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

    public void setBuildSpecificationDao(BuildSpecificationDao buildSpecificationDao)
    {
        this.buildSpecificationDao = buildSpecificationDao;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}