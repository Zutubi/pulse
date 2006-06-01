package com.zutubi.pulse.scheduling.tasks;

import com.zutubi.pulse.SlaveProxyFactory;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.SlaveAvailableEvent;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.scheduling.Task;
import com.zutubi.pulse.scheduling.TaskExecutionContext;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.util.logging.Logger;

/**
 * <class-comment/>
 */
public class PingSlaves implements Task
{
    private static final Logger LOG = Logger.getLogger(PingSlaves.class);

    private SlaveManager slaveManager;
    private SlaveProxyFactory factory;
    private EventManager eventManager;

    public void execute(TaskExecutionContext context)
    {
        LOG.info("pinging slaves.");
        for (Slave slave : slaveManager.getAll())
        {
            long currentTime = System.currentTimeMillis();

            try
            {
                SlaveService service = factory.createProxy(slave);
                service.ping();
                slave.lastPing(currentTime, true);
                eventManager.publish(new SlaveAvailableEvent(this, slave));
            }
            catch (Exception e)
            {
                LOG.info("Ping to slave '" + slave.getName() + "' failed. Exception: '"+e.getClass().getName()+"' Reason: " + e.getMessage());
                slave.lastPing(currentTime, false);
            }
            slaveManager.save(slave);
        }
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }

    public void setSlaveProxyFactory(SlaveProxyFactory factory)
    {
        this.factory = factory;
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }
}