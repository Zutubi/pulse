package com.zutubi.pulse.model;

import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.model.persistence.ScmDao;
import com.zutubi.pulse.scheduling.*;
import com.zutubi.pulse.scm.MonitorScms;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;

import java.util.List;

/**
 *
 *
 */
public class DefaultScmManager implements ScmManager
{
    private static final Logger LOG = Logger.getLogger(DefaultScmManager.class);

    private ScmDao scmDao = null;

    private Scheduler scheduler;
    private static final String MONITOR_NAME = "poll";
    private static final String MONITOR_GROUP = "scm";
    private static final long POLLING_FREQUENCY = Constants.MINUTE;

    private MasterConfigurationManager configManager;

    public void setScmDao(ScmDao scmDao)
    {
        this.scmDao = scmDao;
    }

    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void setConfigurationManager(MasterConfigurationManager configManager)
    {
        this.configManager = configManager;
    }

    public void init()
    {
        // check if the trigger exists. if not, create and schedule.
        Trigger trigger = scheduler.getTrigger(MONITOR_NAME, MONITOR_GROUP);
        if (trigger != null)
        {
            return;
        }

        // initialise the trigger.
        trigger = new SimpleTrigger(MONITOR_NAME, MONITOR_GROUP, POLLING_FREQUENCY);
        trigger.setTaskClass(MonitorScms.class);

        try
        {
            scheduler.schedule(trigger);
        }
        catch (SchedulingException e)
        {
            LOG.severe(e);
        }
    }

    public List<Scm> getActiveScms()
    {
        return scmDao.findAllActive();
    }

    public void save(Scm entity)
    {
        scmDao.save(entity);
    }

    public void delete(Scm entity)
    {
        scmDao.delete(entity);
    }

    public Scm getScm(long id)
    {
        return scmDao.findById(id);
    }

    public int getDefaultPollingInterval()
    {
        return configManager.getAppConfig().getScmPollingInterval();
    }

    public void setDefaultPollingInterval(int interval)
    {
        configManager.getAppConfig().setScmPollingInterval(interval);
    }
}
