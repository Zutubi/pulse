package com.zutubi.pulse.master.scheduling;

import com.zutubi.events.Event;
import com.zutubi.events.EventListener;
import com.zutubi.events.EventManager;
import com.zutubi.pulse.master.model.persistence.TriggerDao;
import com.zutubi.pulse.servercore.events.system.SystemStartedEvent;
import com.zutubi.util.logging.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
     * @param strategy new scheduler strategy implementation
     */
    public void register(SchedulerStrategy strategy)
    {
        for (String key : strategy.canHandle())
        {
            strategies.put(key, strategy);
        }
        strategy.setTriggerHandler(triggerHandler);
    }

    public void setStrategies(SchedulerStrategy... schedulerStrategies)
    {
        setStrategies(Arrays.asList(schedulerStrategies));
    }
    
    public void setStrategies(List<SchedulerStrategy> schedulerStrategies)
    {
        strategies.clear();
        for (SchedulerStrategy strategy : schedulerStrategies)
        {
            register(strategy);
        }
    }

    public void start()
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
                    strategy.init(trigger);
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
        Trigger existingTrigger = triggerDao.findByNameAndGroup(trigger.getName(), trigger.getGroup());
        if (existingTrigger != null && existingTrigger.isScheduled())
        {
            throw new SchedulingException("A trigger with name " + trigger.getName() + " and group " + trigger.getGroup() + " has already been scheduled.");
        }

        if (trigger.isScheduled())
        {
            throw new SchedulingException("Trigger is already scheduled.");
        }

        // assosiate trigger and task so that task can be retrieved when trigger fires.
        trigger.setState(TriggerState.SCHEDULED);
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
     * @param trigger instance to be unscheduled.
     * @throws SchedulingException
     */
    public void unschedule(final Trigger trigger) throws SchedulingException
    {
        assertScheduled(trigger);
        
        trigger.setState(TriggerState.NONE);
        triggerDao.save(trigger);

        SchedulerStrategy impl = getStrategy(trigger);
        impl.unschedule(trigger);

        triggerDao.delete(trigger);
    }

    public void update(Trigger trigger) throws SchedulingException
    {
        assertScheduled(trigger);

        triggerDao.save(trigger);

        TriggerState state = trigger.getState();

        SchedulerStrategy impl = getStrategy(trigger);
        impl.unschedule(trigger);

        switch (state)
        {
            case SCHEDULED:
                impl.schedule(trigger);
                break;
            case PAUSED:
                impl.pause(trigger);
                break;
        }
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

        trigger.setState(TriggerState.PAUSED);
        triggerDao.save(trigger);

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

        trigger.setState(TriggerState.SCHEDULED);
        triggerDao.save(trigger);

        SchedulerStrategy impl = getStrategy(trigger);
        impl.resume(trigger);
    }

    public void renameProjectTriggers(long project, String name) throws SchedulingException
    {
        List<Trigger> projectTriggers = getTriggers(project);
        for(Trigger trigger: projectTriggers)
        {
            SchedulerStrategy impl = getStrategy(trigger);
            boolean active = trigger.isActive();
            if (active)
            {
                impl.unschedule(trigger);
            }
            trigger.setGroup(name);
            triggerDao.save(trigger);
            if (active)
            {
                impl.schedule(trigger);
            }
        }
    }

    public void unscheduleAllTriggers(long project) throws SchedulingException
    {
        List<Trigger> triggers = triggerDao.findAll();
        for(Trigger trigger: triggers)
        {
            SchedulerStrategy impl = getStrategy(trigger);
            if(trigger.getProject() == project || impl.dependsOnProject(trigger, project))
            {
                impl.unschedule(trigger);
                triggerDao.delete(trigger);                
            }
        }
    }

    /**
     * Retrieve the first registered strategy that is able to handle this trigger.
     *
     * @param trigger instance for which the strategy is being retrieved.
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
            throw new SchedulingException("The trigger must be scheduled.");
        }
    }

    /**
     * Set a reference to the required TriggerDao resource.
     *
     * @param triggerDao instance
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
