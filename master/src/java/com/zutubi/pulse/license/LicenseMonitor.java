package com.zutubi.pulse.license;

import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.scheduling.*;
import com.zutubi.pulse.util.logging.Logger;

/**
 * This task is used to periodically check the systems license and generate a
 * LicenseExpiredEvent if the license has expired.
 *
 * @author Daniel Ostermeier
 */
public class LicenseMonitor implements Task
{
    /**
     * This monitors logger.
     */
    private static final Logger LOG = Logger.getLogger(LicenseMonitor.class);

    /**
     * Used to lookup the data -> license.
     */
    private MasterConfigurationManager configurationManager;

    /**
     * Used to deliver the LicenseExpiredEvent when necessary.
     */
    private EventManager eventManager;

    /**
     * The systems scheduler, required so that this monitor can schedule itself during startup.
     */
    private Scheduler scheduler;

    /**
     * Required resource
     *
     * @param configurationManager
     */
    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

    /**
     * Required resource.
     *
     * @param eventManager
     */
    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    /**
     * Required resource.
     *
     * @param scheduler
     */
    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    /**
     *
     * @param context
     */
    public void execute(TaskExecutionContext context)
    {
        License license = configurationManager.getData().getLicense();
        if (license.isExpired())
        {
            eventManager.publish(new LicenseExpiredEvent(license));
        }
    }

    /**
     * Initialise this license monitor, registering it with the scheduler
     * if it has not already been registered.
     *
     */
    public void init()
    {
        // check if the trigger exists. if not, create and schedule.
        Trigger trigger = scheduler.getTrigger("license", "monitor");
        if (trigger != null)
        {
            return;
        }

        // initialise the trigger - trigger at 1am every day.
        trigger = new CronTrigger("0 0 1 * * ?", "license", "monitor");
        trigger.setTaskClass(LicenseMonitor.class);

        try
        {
            scheduler.schedule(trigger);
        }
        catch (SchedulingException e)
        {
            LOG.severe(e);
        }
    }
}
