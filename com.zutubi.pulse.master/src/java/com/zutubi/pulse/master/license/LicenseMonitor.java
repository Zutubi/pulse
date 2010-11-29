package com.zutubi.pulse.master.license;

import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.license.events.LicenseExpiredEvent;
import com.zutubi.pulse.master.scheduling.*;
import com.zutubi.util.logging.Logger;

/**
 * This task is used to periodically check the systems license and generate a
 * LicenseExpiredEvent if the license has expired.
 */
public class LicenseMonitor implements Task
{
    /**
     * This monitors logger.
     */
    private static final Logger LOG = Logger.getLogger(LicenseMonitor.class);

    /**
     * Used to deliver the LicenseExpiredEvent when necessary.
     */
    private EventManager eventManager;

    /**
     * The systems scheduler, required so that this monitor can schedule itself during startup.
     */
    private Scheduler scheduler;

    private LicenseManager licenseManager;

    /**
     * The execution of this task is to check whether or not the license is expired. If the
     * installed license is expired, then generate an appropriate LicenseEvent.
     *
     * @param context trigger execution context
     */
    public void execute(TaskExecutionContext context)
    {
        License license = LicenseHolder.getLicense();
        if (license.isExpired())
        {
            // trigger a refresh of authorisations.
            licenseManager.refreshAuthorisations();
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
        //TODO: We need to ensure that this task can not be unscheduled.

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
            // if there is a problem scheduling this, then license expiry checks will not
            // be run. Should the system startup continue?
            LOG.severe(e);
        }
    }

    public void setEventManager(EventManager eventManager)
    {
        this.eventManager = eventManager;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setLicenseManager(LicenseManager licenseManager)
    {
        this.licenseManager = licenseManager;
    }
}
