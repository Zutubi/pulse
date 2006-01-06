package com.cinnamonbob.model;

import com.cinnamonbob.model.persistence.SlaveDao;
import com.cinnamonbob.scheduling.Scheduler;
import com.cinnamonbob.util.logging.Logger;

import java.util.List;

/**
 * <class-comment/>
 */
public class DefaultSlaveManager implements SlaveManager
{
    private static final Logger LOG = Logger.getLogger(DefaultSlaveManager.class);

    private SlaveDao slaveDao;

    private Scheduler scheduler;

    public void init()
    {
//        // register a schedule for pinging the slaves.
//        // check if the trigger exists. if not, create and schedule.
//        Trigger trigger = scheduler.getTrigger(MONITOR_NAME, MONITOR_GROUP);
//        if (trigger != null)
//        {
//            return;
//        }
//
//        // initialise the trigger.
//        trigger = new SimpleTrigger(MONITOR_NAME, MONITOR_GROUP, POLLING_FREQUENCY);
//        trigger.setTaskClass(MonitorScms.class);
//
//        try
//        {
//            scheduler.schedule(trigger);
//        }
//        catch (SchedulingException e)
//        {
//            LOG.severe(e);
//        }
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
