/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model;

import com.zutubi.pulse.core.util.Constants;
import com.zutubi.pulse.model.persistence.SlaveDao;
import com.zutubi.pulse.scheduling.*;
import com.zutubi.pulse.scheduling.tasks.PingSlaves;
import com.zutubi.pulse.util.logging.Logger;

import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultSlaveManager implements SlaveManager
{
    private static final Logger LOG = Logger.getLogger(DefaultSlaveManager.class);

    private SlaveDao slaveDao;

    private Scheduler scheduler;

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
            slaveDao.delete(slave);
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
}