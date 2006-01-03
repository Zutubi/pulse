package com.cinnamonbob.model;

import com.cinnamonbob.core.util.Constants;
import com.cinnamonbob.model.persistence.ScmDao;
import com.cinnamonbob.scheduling.DefaultScheduler;
import com.cinnamonbob.scheduling.SchedulingException;
import com.cinnamonbob.scheduling.SimpleTrigger;
import com.cinnamonbob.scheduling.Trigger;
import com.cinnamonbob.util.logging.Logger;
import com.cinnamonbob.scm.MonitorScms;

import java.util.List;

/**
 *
 *
 */
public class DefaultScmManager implements ScmManager
{
    private static final Logger LOG = Logger.getLogger(DefaultScmManager.class);

    private ScmDao scmDao = null;

    private DefaultScheduler scheduler;
    private static final String MONITOR_NAME = "poll";
    private static final String MONITOR_GROUP = "scm";
    private static final long POLLING_FREQUENCY = 10 * Constants.MINUTE;

    public void setScmDao(ScmDao scmDao)
    {
        this.scmDao = scmDao;
    }

    public void setScheduler(DefaultScheduler scheduler)
    {
        this.scheduler = scheduler;
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
        return scmDao.findAll();
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
}
