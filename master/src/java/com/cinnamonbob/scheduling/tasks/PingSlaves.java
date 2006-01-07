package com.cinnamonbob.scheduling.tasks;

import com.cinnamonbob.scheduling.Task;
import com.cinnamonbob.scheduling.TaskExecutionContext;
import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.SlaveProxyFactory;
import com.cinnamonbob.model.SlaveManager;
import com.cinnamonbob.model.Slave;
import com.cinnamonbob.services.SlaveService;

/**
 * <class-comment/>
 */
public class PingSlaves implements Task
{
    private static final Logger LOG = Logger.getLogger(PingSlaves.class);

    private SlaveManager slaveManager;
    private SlaveProxyFactory factory;

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
}