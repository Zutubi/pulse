package com.zutubi.pulse.scheduling;

import com.zutubi.pulse.events.Event;
import com.zutubi.pulse.events.EventListener;
import com.zutubi.pulse.events.EventManager;
import com.zutubi.pulse.events.system.SystemStartedEvent;
import com.zutubi.pulse.scheduling.persistence.TriggerDao;
import com.zutubi.pulse.util.logging.Logger;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class DefaultScheduler implements Scheduler, EventListener
{
    private static final Logger LOG = Logger.getLogger(DefaultScheduler.class);

    private Map<String, SchedulerStrategy> strategies = new TreeMap<String, SchedulerStrategy>();

    private TriggerHandler triggerHandler;

    private TriggerDao triggerDao;

    private boolean started = false;

    /**
     * Register a scheduling strategy that will be used to handle a specific trigger type.
     *
     * @param strategy
     */
    public void register(SchedulerStrategy strategy)
    {
        strategies.put(strategy.canHandle(), strategy);
        strategy.setTriggerHandler(triggerHandler);
    }

    public void setStrategies(List<SchedulerStrategy> schedulerStrategies)
    {
        strategies.clear();
        for (SchedulerStrategy strategy : schedulerStrategies)
        {
            register(strategy);
        }
    }

    protected void start()
    {
        try
        {
            // ensure that the strategies are correctly configured.
            for (SchedulerStrategy strategy : strategies.values())
            {
                strategy.setTriggerHandler(triggerHandler);
            }

            // initialise the persisted triggers.
            for (Trigger trigger : triggerDao.findAll())
            {
                SchedulerStrategy strategy = getStrategy(trigger);
                try
                {
                    strategy.schedule(trigger);
                    // WARNING: there is a very brief opportunity here for a paused trigger to
                    // execute.... should close this gap..
                    if (trigger.isPaused())
                    {
                        strategy.pause(trigger);
                    }
                }
                catch (SchedulingException e)
                {
                    // not the fact that this trigger is invalid but do not prevent the rest of the triggers from
                    // being initialisd.
                    LOG.severe("failed to initialise a trigger.", e);
                }
            }
        }
        finally
        {
            started = true;
        }
    }

    public Trigger getTrigger(String name, String group)
    {
        return triggerDao.findByNameAndGroup(name, group);
    }

    public Trigger getTrigger(long id)
    {
        return triggerDao.findById(id);
    }

    public List<Trigger> getTriggers()
    {
        return triggerDao.findAll();
    }

    public List<Trigger> getTriggers(long project)
    {
        return triggerDao.findByProject(project);
    }

    public Trigger getTrigger(long project, String triggerName)
    {
        return triggerDao.findByProjectAndName(project, triggerName);
    }

    public void schedule(Trigger trigger) throws SchedulingException
    {
        if (triggerDao.findByNameAndGroup(trigger.getName(), trigger.getGroup()) != null)
        {
            throw new SchedulingException("A trigger with name " + trigger.getName() + " and group " + trigger.getGroup() + " has already been registered.");
        }

        // assosiate trigger and task so that task can be retrieved when trigger fires.
        triggerDao.save(trigger);

        if (started)
        {
            SchedulerStrategy impl = getStrategy(trigger);
            impl.schedule(trigger);
        }
    }

    public void trigger(Trigger trigger) throws SchedulingException
    {
        triggerHandler.fire(trigger);
    }

    /**
     * Unschedule a trigger. Only scheduled triggers can be unscheduled.
     *
     * @param trigger
     * @throws SchedulingException
     */
    public void unschedule(Trigger trigger) throws SchedulingException
    {
        assertScheduled(trigger);
        SchedulerStrategy impl = getStrategy(trigger);
        impl.unschedule(trigger);
        triggerDao.delete(trigger);
    }

    public void update(Trigger trigger) throws SchedulingException
    {
        assertScheduled(trigger);
        SchedulerStrategy impl = getStrategy(trigger);
        impl.unschedule(trigger);
        triggerDao.save(trigger);
        impl.schedule(trigger);
    }

    public void pause(String group) throws SchedulingException
    {
        for (Trigger trigger : triggerDao.findByGroup(group))
        {
            pause(trigger);
        }
    }

    public void pause(Trigger trigger) throws SchedulingException
    {
        assertScheduled(trigger);
        if (!trigger.isActive())
        {
            return;
        }
        SchedulerStrategy impl = getStrategy(trigger);
        impl.pause(trigger);
    }

    public void resume(String group) throws SchedulingException
    {
        for (Trigger trigger : triggerDao.findByGroup(group))
        {
            resume(trigger);
        }
    }

    public void resume(Trigger trigger) throws SchedulingException
    {
        assertScheduled(trigger);
        if (!trigger.isPaused())
        {
            return;
        }
        SchedulerStrategy impl = getStrategy(trigger);
        impl.resume(trigger);

        // record any state change.
        triggerDao.save(trigger);
    }

    /**
     * Retrieve the first registered strategy that is able to handle this trigger.
     *
     * @param trigger
     * @return a scheduler strategy.
     */
    private SchedulerStrategy getStrategy(Trigger trigger)
    {
        return strategies.get(trigger.getType());
    }

    private void assertScheduled(Trigger trigger) throws SchedulingException
    {
        if (!trigger.isScheduled())
        {
            throw new SchedulingException("The trigger must be scheduled first.");
        }
    }

    /**
     * Set a reference to the required TriggerDao resource.
     *
     * @param triggerDao
     */
    public void setTriggerDao(TriggerDao triggerDao)
    {
        this.triggerDao = triggerDao;
    }

    public void setTriggerHandler(TriggerHandler triggerHandler)
    {
        this.triggerHandler = triggerHandler;
    }

    public void setEventManager(EventManager eventManager)
    {
        eventManager.register(this);
    }

    public void handleEvent(Event evt)
    {
        if (evt instanceof SystemStartedEvent)
        {
            start();
        }
    }

    public Class[] getHandledEvents()
    {
        return new Class[]{SystemStartedEvent.class};
    }

    public void stop(boolean force)
    {
        if (!started)
        {
            // nothing to do here.
            return;
        }

        try
        {
            for (SchedulerStrategy strategy : strategies.values())
            {
                strategy.stop(force);
            }
        }
        finally
        {
            started = false;
        }
    }
}
