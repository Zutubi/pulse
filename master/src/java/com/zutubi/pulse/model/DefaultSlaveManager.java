package com.zutubi.pulse.model;

import com.zutubi.pulse.agent.AgentPingService;
import com.zutubi.pulse.model.persistence.BuildSpecificationDao;
import com.zutubi.pulse.model.persistence.SlaveDao;
import com.zutubi.pulse.scheduling.Scheduler;
import com.zutubi.pulse.scheduling.SchedulingException;
import com.zutubi.pulse.scheduling.SimpleTrigger;
import com.zutubi.pulse.scheduling.Trigger;
import com.zutubi.pulse.scheduling.tasks.PingSlaves;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.util.logging.Logger;

import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultSlaveManager implements SlaveManager
{
    private static final Logger LOG = Logger.getLogger(DefaultSlaveManager.class);

    private SlaveDao slaveDao;
    private BuildSpecificationDao buildSpecificationDao;
    private Scheduler scheduler;
    private ProjectManager projectManager;

    private static final String PING_NAME = "ping";
    private static final String PING_GROUP = "services";

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

    public Slave getSlave(String name)
    {
        return slaveDao.findByName(name);
    }

    public List<Slave> getAll()
    {
        return slaveDao.findAll();
    }

    public Slave getSlave(long id)
    {
        return slaveDao.findById(id);
    }

    public void delete(long id)
    {
        Slave slave = slaveDao.findById(id);
        if (slave != null)
        {
            // Remove all build stages that require this slave explicitly
            List<BuildSpecification> buildSpecs = buildSpecificationDao.findBySlave(slave);
            for(BuildSpecification spec: buildSpecs)
            {
                removeStageReferences(spec.getRoot(), id);
                spec.clearCleanBuildForSlave(id);
                projectManager.save(spec);
            }

            slaveDao.delete(slave);
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

    public void delete(Slave slave)
    {
        slaveDao.delete(slave);
    }

    public void save(Slave slave)
    {
        slaveDao.save(slave);
    }

    public void setSlaveDao(SlaveDao slaveDao)
    {
        this.slaveDao = slaveDao;
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